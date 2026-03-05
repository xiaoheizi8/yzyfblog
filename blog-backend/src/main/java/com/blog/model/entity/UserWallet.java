package com.blog.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户钱包（风月币）。
 *
 * <p>记录用户当前风月币余额及累计收支，用于打赏、签到等积分体系。
 *
 * @author 一朝风月
 */
@Data
@TableName("user_wallet")
public class UserWallet extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 当前风月币余额 */
    private BigDecimal balance;

    /** 累计收入 */
    private BigDecimal totalIncome;

    /** 累计支出 */
    private BigDecimal totalExpense;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime updateTime;
}

