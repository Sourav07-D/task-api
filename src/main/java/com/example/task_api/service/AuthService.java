package com.example.task_api.service;

import com.example.task_api.dto.LoginRequestDTO;
import com.example.task_api.dto.RegisterRequestDTO;
import com.example.task_api.exception.BadRequestException;
import com.example.task_api.exception.CustomNotFoundException;
import com.example.task_api.model.User;
import com.example.task_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private  final AuthenticationManager authenticationManager;

    // REGISTER
    public String register(RegisterRequestDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = new User();

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        // 🔐 HASH PASSWORD
        user.setPassword(
                passwordEncoder.encode(dto.getPassword())
        );

        user.setRoles(Set.of("USER"));
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        return "User registered successfully";
    }

    // LOGIN
//    public String login(LoginRequestDTO dto) {
//
//        User user = userRepository.findByEmail(dto.getEmail())
//                .orElseThrow(() ->
//                        new CustomNotFoundException("User not found"));
//
//        // 🔐 VERIFY PASSWORD
//        boolean valid =
//                passwordEncoder.matches(
//                        dto.getPassword(),
//                        user.getPassword()
//                );
//
//        if (!valid) {
//            throw new BadRequestException("Invalid credentials");
//        }
//
//        return "Login successful";
//    }
    public String login(LoginRequestDTO dto) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getEmail(),
                        dto.getPassword()
                )
        );

        return "Login successful";
    }
}