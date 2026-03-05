package com.blog.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口幂等性注解（基于 Idempotency-Key + Redis）。
 *
 * <p>前端在请求头中携带 Idempotency-Key（如一次点击生成一个 UUID），
 * 后端在 expireSeconds 内只处理一次，其余认为是重复提交。
 *
 * @author 一朝风月
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等 key 的 header 名称。
     */
    String header() default "Idempotency-Key";

    /**
     * 幂等 key 的过期时间（秒）。
     */
    long expireSeconds() default 5L;
}

