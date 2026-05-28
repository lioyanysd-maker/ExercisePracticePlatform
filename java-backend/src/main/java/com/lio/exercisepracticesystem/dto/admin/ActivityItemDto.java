package com.lio.exercisepracticesystem.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityItemDto {
    private String eventType;
    private String text;
    private LocalDateTime occurredAt;
}
