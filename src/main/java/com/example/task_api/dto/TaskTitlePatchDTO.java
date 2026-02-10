package com.example.task_api.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskTitlePatchDTO {

    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;
}
