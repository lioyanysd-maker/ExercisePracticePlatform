package com.lio.exercisepracticesystem.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeTrendDayDto {
    private String label;
    private long practiceCount;
}
