package cn.yzfy.blog.data.net.dto;

/**
 * @author 一朝风月
 * @description 文章 DTO（portal/article 相关接口）。
 * @datetime 2026-03-18
 */
public class Article {
    public Long id;
    public Long userId;
    public String title;
    public String summary;
    public String content;
    public String coverImage;
    public Integer status;
    public Integer topFlag;
    public Long viewCount;
    public String publishTime;
    public String createTime;
    public String updateTime;
}

