package com.lio.exercisepracticesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatisticsResponse {
    private int totalCount;
    private int correctCount;
    private int wrongCount;
    private double accuracy;
    private String grade;
}
