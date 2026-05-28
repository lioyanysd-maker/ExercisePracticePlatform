package com.lio.exercisepracticesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ErrorQuestionResponse {
    private Long errorId;
    private Long questionId;
    private Long subjectId;
    private String type;
    private String question;
    private List<String> options;
    private String answer;
    private String analysis;
    private Integer wrongCount;
    private String lastWrongAt;
}
