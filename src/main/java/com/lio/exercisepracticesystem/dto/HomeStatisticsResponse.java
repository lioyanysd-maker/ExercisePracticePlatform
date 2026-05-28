package com.lio.exercisepracticesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HomeStatisticsResponse {
    private StatisticsResponse today;
    private StatisticsResponse week;
    private StatisticsResponse all;
    private int consecutiveDays;
}
