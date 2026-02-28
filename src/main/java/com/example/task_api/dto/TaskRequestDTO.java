package com.example.task_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskRequestDTO {

    @NotBlank(message = "Task title cannot be empty")
    @Size(
            min = 3,
            max = 100,
            message = "Task title must be between 3 and 100 characters"
    )
    private String title;

    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    private String description;

    @NotBlank(message = "userId is required")
    private String userId;
}

