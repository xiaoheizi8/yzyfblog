package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.UserWallet;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户钱包 Mapper。
 *
 * @author 一朝风月
 */
@Mapper
public interface UserWalletMapper extends BaseMapper<UserWallet> {
}

