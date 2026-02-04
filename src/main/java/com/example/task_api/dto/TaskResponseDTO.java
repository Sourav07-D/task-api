package com.example.task_api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponseDTO {

    private String id;
    private String title;
    private String description;
    private boolean completed;
    private LocalDateTime createdAt;
}
