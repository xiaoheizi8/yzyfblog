package com.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 博客系统启动类
 *
 * <p>启用 Mapper 扫描与定时任务调度。
 *
 * @author 一朝风月
 */
@SpringBootApplication
@MapperScan("com.blog.mapper")
@EnableScheduling
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
