package com.lio.exercisepracticesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteBannerDto {
    private String homeBannerText;
    private boolean homeBannerEnabled;
    private LocalDateTime updatedAt;
}
