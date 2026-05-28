package com.lio.exercisepracticesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private LocalDateTime createdAt;
    private String role;
}

