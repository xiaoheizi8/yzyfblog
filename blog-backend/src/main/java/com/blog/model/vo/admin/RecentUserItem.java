package com.blog.model.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仪表盘-最近用户条目。
 *
 * @author 一朝风月
 */
@Data
public class RecentUserItem {

    /**
     * 用户 ID。
     */
    private Long id;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 昵称。
     */
    private String nickname;

    /**
     * 用户状态。
     */
    private Integer status;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}

