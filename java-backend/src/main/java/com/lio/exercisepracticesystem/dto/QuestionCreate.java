package com.lio.exercisepracticesystem.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionCreate {
    private Long userId;
    private Long subjectId;
    private String type;
    private String question;
    private List<String> options;
    private String answer;
    private String analysis;
}

