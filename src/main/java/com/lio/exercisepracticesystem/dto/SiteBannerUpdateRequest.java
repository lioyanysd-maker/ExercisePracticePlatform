package com.lio.exercisepracticesystem.dto;

import lombok.Data;

@Data
public class SiteBannerUpdateRequest {
    private String homeBannerText;
    private Boolean homeBannerEnabled;
}
