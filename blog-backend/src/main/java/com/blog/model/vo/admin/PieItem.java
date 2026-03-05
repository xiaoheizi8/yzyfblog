package com.blog.model.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 饼图通用项。
 *
 * <p>用于表示名称 + 数值结构的数据，例如用户状态分布、文章状态分布、标签文章数量等。
 *
 * @author 一朝风月
 */
@Data
@AllArgsConstructor
public class PieItem {

    /**
     * 名称（例如：启用用户、草稿、某个标签等）。
     */
    private String name;

    /**
     * 数量值。
     */
    private long value;
}

