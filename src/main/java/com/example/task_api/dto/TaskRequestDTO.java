package com.example.task_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 3, max = 100)
    private String description;
}

