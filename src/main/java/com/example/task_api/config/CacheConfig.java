//package com.example.task_api.config;
//
//import com.github.benmanes.caffeine.cache.Caffeine;
//import org.springframework.cache.CacheManager;
//import org.springframework.cache.caffeine.CaffeineCache;
//import org.springframework.cache.support.SimpleCacheManager;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//// it is not working due to version problem
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//@Configuration
//public class CacheConfig {
//
//    @Bean
//    public CacheManager cacheManager() {
//
//        SimpleCacheManager cacheManager = new SimpleCacheManager();
//
//        // ✅ TASK CACHE (changes frequently)
//        CaffeineCache tasksCache =
//                new CaffeineCache(
//                        "tasks",
//                        Caffeine.newBuilder()
//                                .expireAfterWrite(5, TimeUnit.MINUTES)
//                                .maximumSize(1000)
//                                .recordStats()
//                                .build()
//                );
//
//        // ✅ USER CACHE (changes rarely)
//        CaffeineCache usersCache =
//                new CaffeineCache(
//                        "users",
//                        Caffeine.newBuilder()
//                                .expireAfterWrite(10, TimeUnit.MINUTES)
//                                .maximumSize(500)
//                                .recordStats()
//                                .build()
//                );
//
//        // ✅ LIGHTWEIGHT SUMMARY CACHE
//        CaffeineCache taskSummaryCache =
//                new CaffeineCache(
//                        "taskSummary",
//                        Caffeine.newBuilder()
//                                .expireAfterWrite(3, TimeUnit.MINUTES)
//                                .maximumSize(2000)
//                                .recordStats()
//                                .build()
//                );
//
//        cacheManager.setCaches(
//                List.of(
//                        tasksCache,
//                        usersCache,
//                        taskSummaryCache
//                )
//        );
//
//        return cacheManager;
//    }
//}