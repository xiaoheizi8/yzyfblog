package com.blog.model.vo.portal;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论展示 VO（含用户昵称）。
 *
 * @author 一朝风月
 */
@Data
public class CommentVO {

    private Long id;
    private Long articleId;
    private Long userId;
    private Long parentId;
    private Long replyToId;
    private String content;
    private Integer likeCount;
    /** 评论者昵称（优先显示，无则用 username） */
    private String nickname;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
