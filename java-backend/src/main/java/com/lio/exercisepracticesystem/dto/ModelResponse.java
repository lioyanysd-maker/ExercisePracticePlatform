package com.lio.exercisepracticesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModelResponse {
    private Long id;
    private Long userId;
    private String modelName;
    private String baseUrl;
    private String apiKey;
    private String createdAt;
}
