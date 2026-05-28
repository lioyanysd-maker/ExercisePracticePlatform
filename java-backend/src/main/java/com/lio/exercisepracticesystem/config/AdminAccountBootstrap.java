package com.lio.exercisepracticesystem.config;

import com.lio.exercisepracticesystem.entity.User;
import com.lio.exercisepracticesystem.entity.UserRole;
import com.lio.exercisepracticesystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Component
public class AdminAccountBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.username:}")
    private String bootstrapUsername;

    @Value("${app.bootstrap-admin.password:}")
    private String bootstrapPassword;

    public AdminAccountBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(bootstrapUsername) || !StringUtils.hasText(bootstrapPassword)) {
            return;
        }
        String name = bootstrapUsername.trim();
        if (userRepository.existsByUsername(name)) {
            return;
        }
        User admin = new User();
        admin.setUsername(name);
        admin.setPasswordHash(passwordEncoder.encode(bootstrapPassword));
        admin.setRole(UserRole.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);
    }
}
