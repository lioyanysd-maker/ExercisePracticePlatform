package com.lio.exercisepracticesystem.controller;

import com.lio.exercisepracticesystem.dto.admin.AdminDashboardSummaryDto;
import com.lio.exercisepracticesystem.service.AdminDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/summary")
    public AdminDashboardSummaryDto summary(
            @RequestParam(value = "trend_days", defaultValue = "7") int trendDays) {
        return adminDashboardService.buildSummary(trendDays);
    }
}
