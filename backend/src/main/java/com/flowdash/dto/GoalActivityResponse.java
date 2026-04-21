package com.flowdash.dto;

import java.time.Instant;
import java.time.LocalDate;

public record GoalActivityResponse(
        Long id,
        Long goalId,
        LocalDate activityDate,
        String note,
        Instant createdAt,
        Instant updatedAt
) {
}
