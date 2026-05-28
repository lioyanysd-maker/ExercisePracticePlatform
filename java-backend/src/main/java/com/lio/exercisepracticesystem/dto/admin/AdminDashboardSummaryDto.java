package com.lio.exercisepracticesystem.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardSummaryDto {
    private AdminOverviewDto overview;
    private List<PracticeTrendDayDto> practiceTrend;
    private List<ActivityItemDto> activity;
}
