package com.lio.exercisepracticesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShareResponse {
    private Long id;
    private Long subjectId;
    private Long targetUserId;
    private String targetUsername;
    private String shareType;
    private String createdAt;
}
