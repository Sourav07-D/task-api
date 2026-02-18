package com.example.task_api.mapper;

import com.example.task_api.dto.UserRequestDTO;
import com.example.task_api.dto.UserResponseDTO;
import com.example.task_api.dto.UserSummaryDTO;
import com.example.task_api.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    // ✅ Create DTO → Entity
    public static User toEntity(UserRequestDTO dto) {

        if(dto==null) return  null;
        User user=new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    // ✅ Entity → Response DTO
    public static UserResponseDTO toResponseDTO(User user) {

        if (user == null) return null;

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt());
    }

    // ✅ Entity List → DTO List
    public static List<UserResponseDTO> toResponseList(List<User> users) {

        if (users == null) return List.of();

        return users.stream()
                .map(UserMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public static UserSummaryDTO toSummaryDTO(User user) {

        if (user == null) return null;

        return new UserSummaryDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

}
