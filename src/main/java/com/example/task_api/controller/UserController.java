package com.example.task_api.controller;

import com.example.task_api.dto.ApiResponse;
import com.example.task_api.dto.UserRequestDTO;
import com.example.task_api.dto.UserResponseDTO;
import com.example.task_api.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ✅ POST /users
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(
            @Valid @RequestBody UserRequestDTO dto) {

        UserResponseDTO response =
                userService.createUser(dto);

        return ResponseEntity.status(201).body(
                ApiResponse.<UserResponseDTO>builder()
                        .success(true)
                        .message("User created")
                        .data(response)
                        .build()
        );
    }

    // ✅ GET /users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(
            @PathVariable String id) {

        UserResponseDTO response =
                userService.getUserById(id);

        return ResponseEntity.ok(
                ApiResponse.<UserResponseDTO>builder()
                        .success(true)
                        .message("User fetched")
                        .data(response)
                        .build()
        );
    }
}
