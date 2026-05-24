package cn.yzfy.blog.data.net.dto;

/**
 * @author 一朝风月
 * @description 评论 DTO（用于 portal/comment 接口）。
 * @datetime 2026-03-19
 */
public class Comment {
    public Long id;
    public Long articleId;
    public Long userId;
    public Long parentId;
    public Long replyToId;
    public String content;
    public Integer likeCount;
    /** 评论者昵称（后端优先返回，无则前端用 userId 兜底） */
    public String nickname;
    public String createTime;
}

