package com.example.task_api.controller;

import com.example.task_api.dto.LoginRequestDTO;
import com.example.task_api.dto.LoginResponseDTO;
import com.example.task_api.dto.RegisterRequestDTO;
import com.example.task_api.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<String>> register(
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
}