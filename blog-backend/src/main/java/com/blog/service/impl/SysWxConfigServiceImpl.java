package com.blog.service.impl;

import com.blog.mapper.SysWxConfigMapper;
import com.blog.model.dto.WxConfigDTO;
import com.blog.model.entity.SysWxConfig;
import com.blog.service.SysWxConfigService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @className: SysWxConfigSeviceImpl
 * @description: 提供给uniapp端的微信配置检阅
 * @author: 风不止
 * @code: 面向自己, 面向未来
 * @createTime: 2026/3/21 9:16
 */
@Service
 public class SysWxConfigServiceImpl implements SysWxConfigService {
    @Lazy
    @Resource
    private   SysWxConfigMapper sysWxConfigMapper;


    @Override
    public SysWxConfig getWxConfig(String winterfly) {
        return sysWxConfigMapper.getWxConfig(winterfly);
    }

    @Override
    public SysWxConfig getWxConfig() {
         return sysWxConfigMapper.selectOne(null);
    }
    @Transactional
    @Override
    public  Integer updateConfigInteger(WxConfigDTO wxConfigDTO) {
      Integer res=  sysWxConfigMapper.updateConfigInteger(wxConfigDTO.getWinterfly(),wxConfigDTO.getPwd(),wxConfigDTO.getId());
      return res;


    }
}
