package com.blog.model.vo.admin;

import lombok.Data;

/**
 * 管理端-运行时系统信息 VO。
 *
 * <p>用于在仪表盘展示网站运行时间、数据库与缓存中间件等信息。
 *
 * @author 一朝风月
 */
@Data
public class RuntimeInfoVO {

    /**
     * 应用启动时间（ISO 字符串）。
     */
    private String startTime;

    /**
     * 已运行时长描述，如 "1天2小时15分钟"。
     */
    private String uptime;

    /**
     * MySQL 连接状态：UP / DOWN。
     */
    private String mysqlStatus;

    /**
     * MySQL 版本信息。
     */
    private String mysqlVersion;

    /**
     * Redis 连接状态：UP / DOWN。
     */
    private String redisStatus;

    /**
     * Redis 版本信息。
     */
    private String redisVersion;
}

