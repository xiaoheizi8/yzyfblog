package cn.yzfy.blog.data.net.dto;

/**
 * @author 一朝风月
 * @description 留言/弹幕墙 DTO（portal/message 相关接口）。
 * @datetime 2026-03-20
 */
public class Message {
    public Long id;
    public Long userId;
    public String content;
    public String createTime;
}
