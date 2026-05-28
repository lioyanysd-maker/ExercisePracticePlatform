package com.lio.exercisepracticesystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "llm_models")
public class LlmModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "model_name", nullable = false, length = 255)
    private String modelName;

    @Column(name = "base_url", nullable = false, length = 255)
    private String baseUrl;

    @Column(name = "api_key", nullable = false, length = 255)
    private String apiKey;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
