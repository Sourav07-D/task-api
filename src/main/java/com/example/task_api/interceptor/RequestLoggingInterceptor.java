package com.example.task_api.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log =
            LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        request.setAttribute(START_TIME,
                System.currentTimeMillis());

        log.info("Incoming Request → {} {}",
                request.getMethod(),
                request.getRequestURI());

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {

        long start =
                (long) request.getAttribute(START_TIME);

        long duration =
                System.currentTimeMillis() - start;

        log.info(
                "Completed → {} {} | Status: {} | Time: {} ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration
        );
    }
}
