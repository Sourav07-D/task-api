package com.example.task_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditInfo {

    private String createdByUserId;
    private LocalDateTime createdAt;

    private LocalDateTime lastModifiedAt;
    private String lastModifiedByUserId;
}

