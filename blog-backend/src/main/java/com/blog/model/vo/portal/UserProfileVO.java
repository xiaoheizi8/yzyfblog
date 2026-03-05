package com.blog.model.vo.portal;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门户-用户个人资料 VO。
 *
 * <p>用于小程序 / H5 展示当前登录用户的基本信息。
 *
 * @author 一朝风月
 */
@Data
public class UserProfileVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    /**
     * 注册时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}

