package com.example.task_api.service;

import com.example.task_api.exception.CustomNotFoundException;
import com.example.task_api.model.User;
import com.example.task_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AsyncUserService {

    private final UserRepository userRepository;

    private static final Logger log =
            LoggerFactory.getLogger(AsyncUserService.class);

    @Async("taskExecutor")
    public CompletableFuture<User> fetchUserAsync(String userId) {

        log.info("Fetching user async → {} | Thread: {}",
                userId,
                Thread.currentThread().getName());

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomNotFoundException("User not found"));

        return CompletableFuture.completedFuture(user);
    }
}
