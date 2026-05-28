package com.lio.exercisepracticesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse {
    private Long id;
    private String name;
    private Long userId;
    private String createdAt;
    private Boolean isOwner;
    private Boolean isShared;
    private String ownerUsername;
    private String shareType;
    private Boolean hasShared;
}

