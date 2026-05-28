package com.lio.exercisepracticesystem.dto;

import lombok.Data;

@Data
public class SetShareRequest {
    private Long ownerUserId;
    private Long subjectId;
    private Long targetUserId;
    private String shareType;
}
