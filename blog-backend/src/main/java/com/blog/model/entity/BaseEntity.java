package com.blog.model.entity;

import java.io.Serializable;

/**
 * 实体基类，统一实现序列化接口。
 *
 * <p>所有持久化实体建议继承此类，便于缓存与远程调用使用。
 *
 * @author 一朝风月
 */
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;
}

