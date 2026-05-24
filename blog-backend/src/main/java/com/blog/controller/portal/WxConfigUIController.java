package com.blog.controller.portal;

import com.blog.common.Result;
import com.blog.model.entity.SysWxConfig;
import com.blog.service.SysWxConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @className: WxConfigUIController
 * @description: 描述该类的功能
 * @author: 风不止
 * @code: 面向自己, 面向未来
 * @createTime: 2026/3/21 9:19
 */
@RestController
@RequestMapping("/wx")
@RequiredArgsConstructor
@Slf4j
public class WxConfigUIController {
    @Lazy
    private final SysWxConfigService sysWxConfigService;

    @GetMapping("/queryForConfig")
    public Result<SysWxConfig> queryForConfig() {
        //当前只取一条记录
        SysWxConfig winterfly=sysWxConfigService.getWxConfig();
        if (StringUtils.isEmpty(winterfly.getConfig())||winterfly.getConfig().equals("")){
            return Result.fail("未配置");
        }

        return Result.ok(winterfly);



    }
}
