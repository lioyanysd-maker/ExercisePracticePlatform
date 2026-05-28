package com.lio.exercisepracticesystem.dto;

import lombok.Data;

@Data
public class ModelCreate {
    private Long userId;
    private String modelName;
    private String baseUrl;
    private String apiKey;
}
