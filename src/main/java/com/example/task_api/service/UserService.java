package com.example.task_api.service;

import com.example.task_api.dto.UserRequestDTO;
import com.example.task_api.dto.UserResponseDTO;
import com.example.task_api.dto.UserSummaryDTO;
import com.example.task_api.exception.BadRequestException;
import com.example.task_api.exception.CustomNotFoundException;
import com.example.task_api.mapper.UserMapper;
import com.example.task_api.model.User;
import com.example.task_api.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private static final Logger log =
            LoggerFactory.getLogger(UserService.class);

    // =====================================================
    // ✅ CREATE USER
    // =====================================================

    public UserResponseDTO createUser(UserRequestDTO dto) {

        log.info("Creating user with email: {}", dto.getEmail());

        // uniqueness validation
        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Duplicate email attempt: {}", dto.getEmail());
            throw new BadRequestException("Email already exists");
        }

        User user = UserMapper.toEntity(dto);

        User saved = userRepository.save(user);

        return UserMapper.toResponseDTO(saved);
    }

    // =====================================================
    // ✅ CACHEABLE READ — FULL USER
    // =====================================================

    @Cacheable(value = "users", key = "#id")
    public UserResponseDTO getUserById(String id) {

        log.info("Fetching USER from DB with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new CustomNotFoundException(
                                "User not found with id: " + id));

        return UserMapper.toResponseDTO(user);
    }

    // =====================================================
    // ✅ CACHEABLE READ — SUMMARY (USED BY TASK SERVICE)
    // =====================================================

    @Cacheable(value = "users", key = "#id")
    public UserSummaryDTO getUserSummary(String id) {

        log.info("Fetching USER SUMMARY from DB with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new CustomNotFoundException(
                                "User not found with id: " + id));

        return UserMapper.toSummaryDTO(user);
    }

    // =====================================================
    // ⭐ NEW — UPDATE USER (CACHE EVICTION)
    // =====================================================

    /**
     * CHANGE ⭐
     * Evicts cached user after update
     */
    @CacheEvict(value = "users", key = "#id")
    public UserResponseDTO updateUser(
            String id,
            UserRequestDTO dto) {

        log.info("Updating user {}, evicting cache", id);

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new CustomNotFoundException(
                                "User not found with id: " + id));

        // email uniqueness check
        if (!user.getEmail().equals(dto.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {

            throw new BadRequestException(
                    "Email already exists");
        }

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        User saved = userRepository.save(user);

        return UserMapper.toResponseDTO(saved);
    }

    // =====================================================
    // ⭐ NEW — DELETE USER (CACHE EVICTION)
    // =====================================================

    /**
     * CHANGE ⭐
     * Prevent stale cached users
     */
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(String id) {

        log.info("Deleting user {}, evicting cache", id);

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new CustomNotFoundException(
                                "User not found with id: " + id));

        userRepository.delete(user);
    }
}