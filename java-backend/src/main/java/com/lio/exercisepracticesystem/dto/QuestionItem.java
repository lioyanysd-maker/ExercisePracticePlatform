package com.lio.exercisepracticesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuestionItem {
    private Long id;
    private String type;
    private String question;
    private List<String> options;
}

