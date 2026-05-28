package com.lio.exercisepracticesystem.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PracticeRequest {
    private Long userId;
    private Long subjectId;
    private Map<String, Integer> questionCounts;
}

