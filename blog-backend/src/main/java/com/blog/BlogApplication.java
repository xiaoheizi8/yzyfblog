package com.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
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
public class BlogApplication extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {

        return builder.sources(BlogApplication.class);
    }



    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
