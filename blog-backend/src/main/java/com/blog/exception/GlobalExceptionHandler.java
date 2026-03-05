package com.blog.exception;

import com.blog.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 *
 * @author blog
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntime(RuntimeException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(500, e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b).orElse("参数校验失败");
        return Result.fail(400, msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handle(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "系统繁忙，请稍后重试");
    }
}
