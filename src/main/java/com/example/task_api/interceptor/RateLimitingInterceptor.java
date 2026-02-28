package com.example.task_api.interceptor;

import com.example.task_api.exception.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS = 60; // per minute
    private static final long WINDOW_SIZE = 60; // seconds

    private final Map<String, RequestInfo> requestCounts =
            new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        String ip = request.getRemoteAddr();

        long currentTime = Instant.now().getEpochSecond();

        RequestInfo info = requestCounts.getOrDefault(
                ip,
                new RequestInfo(0, currentTime)
        );

        if (currentTime - info.startTime >= WINDOW_SIZE) {
            info = new RequestInfo(1, currentTime);
        } else {
            info.count++;
        }

        if (info.count > MAX_REQUESTS) {
            throw new TooManyRequestsException(
                    "Too many requests. Please try again later."
            );
        }

        requestCounts.put(ip, info);

        return true;
    }

    private static class RequestInfo {
        int count;
        long startTime;

        RequestInfo(int count, long startTime) {
            this.count = count;
            this.startTime = startTime;
        }
    }
}