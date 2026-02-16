package com.example.task_api.service;

import com.example.task_api.dto.UserRequestDTO;
import com.example.task_api.dto.UserResponseDTO;
import com.example.task_api.exception.BadRequestException;
import com.example.task_api.exception.CustomNotFoundException;
import com.example.task_api.mapper.UserMapper;
import com.example.task_api.model.User;
import com.example.task_api.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private static final Logger log =
            LoggerFactory.getLogger(UserService.class);

    // ✅ createUser
    public UserResponseDTO createUser(UserRequestDTO dto) {

        log.info("Creating user with email: {}", dto.getEmail());

        // ✅ uniqueness rule
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = UserMapper.toEntity(dto);

        User saved = userRepository.save(user);

        return UserMapper.toResponseDTO(saved);
    }

    // ✅ getUserById
    public UserResponseDTO getUserById(String id) {

        log.info("Fetching user → id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new CustomNotFoundException(
                                "User not found with id: " + id));

        return UserMapper.toResponseDTO(user);
    }
}

