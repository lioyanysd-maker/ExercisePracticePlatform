package com.lio.exercisepracticesystem.dto;

import lombok.Data;

@Data
public class ResourceCreate {
    private Long questionId;
    private String resourceType;
    private String resourceContent;
    private Integer resourceOrder;
}
