package com.flowdash.dto;

import com.flowdash.domain.GoalStatus;

import java.time.Instant;
import java.time.LocalDate;

public record GoalOverviewItemResponse(
        Long id,
        String title,
        String description,
        GoalStatus status,
        Integer priority,
        LocalDate dueDate,
        boolean active,
        boolean progressedToday,
        boolean overdue,
        int currentStreak,
        int bestStreak,
        int weeklyConsistency,
        int monthlyConsistency,
        int yearlyProgressDays,
        Instant createdAt,
        Instant updatedAt
) {
}
