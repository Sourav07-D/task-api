package com.example.task_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AuditInfoDTO {

    private String createdByUserId;
    private LocalDateTime createdAt;

    private LocalDateTime lastModifiedAt;
    private String lastModifiedByUserId;
}
