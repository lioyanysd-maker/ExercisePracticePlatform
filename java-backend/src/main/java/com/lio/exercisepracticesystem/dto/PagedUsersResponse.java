package com.lio.exercisepracticesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PagedUsersResponse {
    private List<UserResponse> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
