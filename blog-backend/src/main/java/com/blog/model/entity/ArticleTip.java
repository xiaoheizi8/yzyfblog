package com.blog.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 文章打赏记录。
 *
 * <p>记录每一次打赏的来源用户、目标作者、文章及金额。
 *
 * @author 一朝风月
 */
@Data
@TableName("article_tip")
public class ArticleTip extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 打赏人 */
    private Long fromUserId;

    /** 作者 */
    private Long toUserId;

    /** 文章ID */
    private Long articleId;

    /** 打赏金额 */
    private BigDecimal amount;

    /** 作者实际收入（amount * 比例） */
    private BigDecimal realIncome;
    //年月日时分秒
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")


    private LocalDateTime createTime;
}

