package com.lio.exercisepracticesystem.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminOverviewDto {
    private long totalUsers;
    private long totalSubjects;
    private long totalQuestions;
    private long totalErrorBookEntries;
    private long todayPracticeSessions;
    private long todayActiveUsers;
}
