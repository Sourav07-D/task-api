package com.example.task_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskStatusPatchDTO {

    @NotNull(message = "Status is required")
    private Boolean completed;
}

