package com.flowdash.dto;

import com.flowdash.domain.GoalStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record GoalRequest(
        @NotBlank String title,
        String description,
        GoalStatus status,
        Integer priority,
        LocalDate dueDate
) {
}
