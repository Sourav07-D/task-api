package com.example.task_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserResponseDTO {

    private String id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
}
