package cn.yzfy.blog.data.net.dto;

import java.util.List;

/**
 * @author 一朝风月
 * @description 分页结果（与后端 PageResult<T> 对齐）。
 * @datetime 2026-03-18
 */
public class PageResult<T> {
    public Long total;
    public Long current;
    public Long size;
    public List<T> records;
}

