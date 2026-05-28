package com.lio.exercisepracticesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResourceResponse {
    private Long id;
    private Long questionId;
    private String resourceType;
    private String resourceContent;
    private Integer resourceOrder;
    private String createdAt;
}
