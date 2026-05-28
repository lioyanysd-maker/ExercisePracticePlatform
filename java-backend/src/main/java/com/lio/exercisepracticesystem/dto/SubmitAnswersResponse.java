package com.lio.exercisepracticesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class SubmitAnswersResponse {
    private int total;
    private int correct;
    private int wrong;
    private double accuracy;
    private String grade;
    private List<Map<String, Object>> results;
}

