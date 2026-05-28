package com.lio.exercisepracticesystem.dto;

import lombok.Data;

@Data
public class AdminUserCreateRequest {
    private String username;
    private String password;
    private String role;
}
