package com.blog.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类表
 *
 * @author blog
 */
@Data
@TableName("blog_category")
public class Category extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Integer sortOrder;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
