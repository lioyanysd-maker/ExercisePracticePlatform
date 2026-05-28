package com.lio.exercisepracticesystem.controller;

import com.lio.exercisepracticesystem.dto.LoginRequest;
import com.lio.exercisepracticesystem.dto.LoginResponse;
import com.lio.exercisepracticesystem.dto.RegisterRequest;
import com.lio.exercisepracticesystem.dto.UserResponse;
import com.lio.exercisepracticesystem.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public LoginResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        String username = authentication.getName();
        return authService.currentUser(username);
    }

    @PostMapping("/logout")
    public String logout() {
        return "登出成功";
    }
}

