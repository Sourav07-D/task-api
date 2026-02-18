package com.example.task_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSummaryDTO {

    private String id;
    private String name;
    private String email;
}
