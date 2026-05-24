package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.model.dto.WxConfigDTO;
import com.blog.service.SysWxConfigService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @className: AdminAuthUniappController
 * @description: 管理端更新配置
 * @author: 风不止
 * @code: 面向自己, 面向未来
 * @createTime: 2026/3/21 9:06
 */

@RestController
@RequestMapping("/wx")
public class AdminAuthWxController {

    @Lazy
    @Resource
    private SysWxConfigService sysWxConfigService;
    //针对于微信小程序端无法审核的问题由后台人员控制

    @PostMapping("/config")
    public Result<?> config(@RequestBody WxConfigDTO wxConfigDTO) {
        if (StringUtils.isEmpty(wxConfigDTO.getWinterfly())){
            return Result.fail("请输入小程序控制人");
        }
        Integer i = sysWxConfigService.updateConfigInteger(wxConfigDTO);
        if (i>0){
            return Result.ok(i);
        }
        return Result.fail("修改失败");

    }



}
