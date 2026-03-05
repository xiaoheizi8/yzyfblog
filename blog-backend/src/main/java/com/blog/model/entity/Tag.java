package com.blog.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签表
 *
 * @author blog
 */
@Data
@TableName("blog_tag")
public class Tag extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String slug;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime createTime;
    @TableLogic
    private Integer deleted;
}
