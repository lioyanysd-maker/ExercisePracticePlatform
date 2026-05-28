package com.lio.exercisepracticesystem.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionUpdate {
    private String type;
    private String question;
    private List<String> options;
    private String answer;
    private String analysis;
}
