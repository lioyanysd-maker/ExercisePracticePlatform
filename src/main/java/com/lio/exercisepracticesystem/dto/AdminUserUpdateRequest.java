package com.lio.exercisepracticesystem.dto;

import lombok.Data;

@Data
public class AdminUserUpdateRequest {
    private String username;
    private String password;
    private String role;
}
