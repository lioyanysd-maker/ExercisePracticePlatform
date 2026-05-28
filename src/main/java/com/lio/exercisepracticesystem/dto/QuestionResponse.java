package com.lio.exercisepracticesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuestionResponse {
    private Long id;
    private Long subjectId;
    private Long userId;
    private String type;
    private String question;
    private List<String> options;
    private String answer;
    private String analysis;
    private String createdAt;
}

