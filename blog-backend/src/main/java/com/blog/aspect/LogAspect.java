package com.blog.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.blog.annotation.Log;
import com.blog.model.entity.OperationLog;
import com.blog.mapper.OperationLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * 操作日志 AOP：对带 @Log 的方法记录入参、耗时、IP 等
 *
 * @author blog
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(controllerLog)")
    public Object around(ProceedingJoinPoint point, Log controllerLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result;
        try {
            result = point.proceed();
        } catch (Throwable e) {
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;
            saveLog(point, controllerLog, duration);
        }
        return result;
    }

    private void saveLog(ProceedingJoinPoint point, Log controllerLog, long duration) {
        try {
            OperationLog opLog = new OperationLog();
            opLog.setModule(controllerLog.module());
            opLog.setOperation(controllerLog.operation());
            if (point.getSignature() instanceof MethodSignature ms) {
                opLog.setMethod(ms.getDeclaringTypeName() + "#" + ms.getName());
            }
            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                try {
                    opLog.setParams(objectMapper.writeValueAsString(Arrays.asList(args)));
                } catch (Exception e) {
                    opLog.setParams(Arrays.toString(args));
                }
            }
            opLog.setDuration(duration);
            if (StpUtil.isLogin()) {
                opLog.setUserId(StpUtil.getLoginIdAsLong());
            }
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                if (request != null) {
                    opLog.setIp(getClientIp(request));
                }
            }
            operationLogMapper.insert(opLog);
        } catch (Exception e) {
            log.warn("记录操作日志失败", e);
        }
    }

    private static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
