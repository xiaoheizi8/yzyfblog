package com.blog.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包流水。
 *
 * <p>记录风月币的每一次变动，便于对账与审计。
 *
 * @author 一朝风月
 */
@Data
@TableName("wallet_transaction")
public class WalletTransaction extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 金额，收入为正，支出为负 */
    private BigDecimal amount;

    /** 流水类型：SIGN_IN / POST / LIKE_REWARD / TIP_IN / TIP_OUT / ADJUST 等 */
    private String type;

    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime createTime;
}

