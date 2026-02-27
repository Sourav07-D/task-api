package com.example.task_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // ⭐ Core thread pool size
        executor.setCorePoolSize(5);

        // ⭐ Max threads allowed
        executor.setMaxPoolSize(10);

        // ⭐ Queue before creating new threads
        executor.setQueueCapacity(50);

        // ⭐ Thread naming (very useful in logs)
        executor.setThreadNamePrefix("async-");

        executor.initialize();

        return executor;
    }
}