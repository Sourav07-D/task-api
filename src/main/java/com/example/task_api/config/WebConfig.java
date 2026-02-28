package com.example.task_api.config;

import com.example.task_api.interceptor.RateLimitingInterceptor;
import com.example.task_api.interceptor.RequestLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor requestLoggingInterceptor;
    private final RateLimitingInterceptor rateLimitingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/api/**");

        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/api/**");
    }
}