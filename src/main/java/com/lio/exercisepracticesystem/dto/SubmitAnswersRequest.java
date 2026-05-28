package com.lio.exercisepracticesystem.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmitAnswersRequest {
    private Long userId;
    private Long subjectId;
    private List<AnswerItem> answers;
}

