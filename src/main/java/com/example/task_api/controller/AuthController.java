package com.example.task_api.controller;

import com.example.task_api.dto.*;
import com.example.task_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO dto) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "User registered",
                        authService.register(dto)
                )
        );
    }

    // LOGIN
    @PostMapping("/login")
    public ApiResponse<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {

        return ApiResponse.success(authService.login(dto));
    }
    @PostMapping("/refresh")
    public ApiResponse<LoginResponseDTO> refresh(
            @RequestBody RefreshRequestDTO dto) {

        return ApiResponse.success(
                authService.refreshToken(dto)
        );
    }
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestBody RefreshRequestDTO request) {

        authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully")
        );
    }
}