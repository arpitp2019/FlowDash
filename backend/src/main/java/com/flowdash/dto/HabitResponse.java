package com.flowdash.dto;

import com.flowdash.domain.HabitCadence;

import java.time.Instant;

public record HabitResponse(
        Long id,
        String title,
        HabitCadence cadence,
        Integer targetCount,
        Integer streak,
        Integer completedCount,
        String notes,
        boolean archived,
        Instant createdAt,
        Instant updatedAt
) {
}
