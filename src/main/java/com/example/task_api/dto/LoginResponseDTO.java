package com.example.task_api.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponseDTO {

    private String accessToken;
    private String refreshToken;
}