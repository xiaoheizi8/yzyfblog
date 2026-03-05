package com.blog.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 *
 * @author blog
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private long total;
    private long current;
    private long size;
    private List<T> records;

    public static <T> PageResult<T> of(long total, long current, long size, List<T> records) {
        PageResult<T> r = new PageResult<>();
        r.setTotal(total);
        r.setCurrent(current);
        r.setSize(size);
        r.setRecords(records);
        return r;
    }
}
