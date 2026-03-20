package com.example.task_api.service;

import com.example.task_api.dto.*;
import com.example.task_api.exception.BadRequestException;
import com.example.task_api.model.RefreshToken;
import com.example.task_api.model.User;
import com.example.task_api.repository.UserRepository;
import com.example.task_api.security.CustomUserDetails;
import com.example.task_api.security.JwtService;
import com.example.task_api.security.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import com.example.task_api.security.CustomUserDetails;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;


    // REGISTER
    public UserResponseDTO register(RegisterRequestDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = new User();

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        // 🔐 HASH PASSWORD
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        user.setRole(Role.ADMIN);
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        return new UserResponseDTO(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getCreatedAt()
        );
    }

    // LOGIN
    public LoginResponseDTO login(LoginRequestDTO dto) {

        // 🔐 Authenticate credentials
        Authentication auth =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                dto.getEmail(),
                                dto.getPassword()
                        )
                );

        // 🔑 Extract authenticated user
        CustomUserDetails user =
                (CustomUserDetails) auth.getPrincipal();

        // 🟢 Generate ACCESS TOKEN (expects UserDetails)
        String accessToken =
                jwtService.generateToken(user);

        // 🟡 Generate REFRESH TOKEN (DB stored)
        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(
                        user.getUsername()
                );

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }
    public LoginResponseDTO refreshToken(
            RefreshRequestDTO request) {

        RefreshToken token =
                refreshTokenService.verifyToken(
                        request.getRefreshToken());

        String userEmail = token.getUserEmail();

        // 🔑 Load user details (needed for JWT)
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(userEmail);

        String newAccessToken =
                jwtService.generateToken(userDetails);

        return LoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(token.getToken())
                .build();
    }
    public void logout(String refreshToken) {

        // delete refresh token from DB
        refreshTokenService.revokeByToken(refreshToken);
    }
}