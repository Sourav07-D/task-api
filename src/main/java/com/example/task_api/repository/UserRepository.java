package com.example.task_api.repository;

import com.example.task_api.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    // ✅ uniqueness check helper
    boolean existsByEmail(String email);
}

