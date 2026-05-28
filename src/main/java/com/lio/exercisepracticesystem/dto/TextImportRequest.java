package com.lio.exercisepracticesystem.dto;

import lombok.Data;

@Data
public class TextImportRequest {
    private Long userId;
    private Long subjectId;
    private String text;
}
