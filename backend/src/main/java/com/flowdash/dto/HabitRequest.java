package com.flowdash.dto;

import com.flowdash.domain.HabitCadence;
import jakarta.validation.constraints.NotBlank;

public record HabitRequest(
        @NotBlank String title,
        HabitCadence cadence,
        Integer targetCount,
        Integer streak,
        Integer completedCount,
        String notes,
        Boolean archived
) {
}
