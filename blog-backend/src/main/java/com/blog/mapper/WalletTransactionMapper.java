package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.WalletTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 钱包流水 Mapper。
 *
 * @author 一朝风月
 */
@Mapper
public interface WalletTransactionMapper extends BaseMapper<WalletTransaction> {
}

