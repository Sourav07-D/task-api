package com.example.task_api.service;

import com.example.task_api.dto.LoginRequestDTO;
import com.example.task_api.dto.LoginResponseDTO;
import com.example.task_api.dto.RegisterRequestDTO;
import com.example.task_api.dto.UserResponseDTO;
import com.example.task_api.exception.BadRequestException;
import com.example.task_api.model.User;
import com.example.task_api.repository.UserRepository;
import com.example.task_api.security.JwtService;
import com.example.task_api.security.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

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

        user.setRole(Role.USER);
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

        // 🔐 Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getEmail(),
                        dto.getPassword()
                )
        );

        // Load UserDetails
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(dto.getEmail());

        // Generate JWT
        String token = jwtService.generateToken(userDetails);

        return new LoginResponseDTO(token);
    }
}