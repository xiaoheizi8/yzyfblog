package com.blog.config;

import com.blog.context.RequestContext;
import com.blog.context.RequestContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步线程池配置。
 *
 * <p>通过 TaskDecorator 将 {@link RequestContext} 从父线程透传到子线程，便于在异步任务中获取用户信息。
 *
 * @author 一朝风月
 */
@Slf4j
@Configuration
public class AsyncConfig {

    @Bean("coinExecutor")
    public Executor coinExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("coin-exec-");
        executor.setTaskDecorator(contextTaskDecorator());
        executor.initialize();
        return executor;
    }

    @Bean
    public TaskDecorator contextTaskDecorator() {
        return runnable -> {
            RequestContext parent = RequestContextHolder.get();
            return () -> {
                try {
                    if (parent != null) {
                        RequestContextHolder.set(parent);
                    }
                    runnable.run();
                } finally {
                    RequestContextHolder.clear();
                }
            };
        };
    }
}

