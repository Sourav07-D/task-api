package com.example.task_api.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
public class TaskResponseDTO {

    private String id;
    private String title;
    private String description;
    private boolean completed;
    private LocalDateTime createdAt;

    private String userId;

}
