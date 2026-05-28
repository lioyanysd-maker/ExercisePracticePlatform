package com.lio.exercisepracticesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PracticeHistoryItem {
    private Long sessionId;
    private String practiceDate;
    private int total;
    private int correct;
    private int wrong;
    private double accuracy;
    private String grade;
    private String subjectName;
}
