package com.flowdash.dto;

import com.flowdash.domain.GoalStatus;

import java.time.Instant;
import java.time.LocalDate;

public record GoalResponse(
        Long id,
        String title,
        String description,
        GoalStatus status,
        Integer priority,
        LocalDate dueDate,
        Instant createdAt,
        Instant updatedAt
) {
}
