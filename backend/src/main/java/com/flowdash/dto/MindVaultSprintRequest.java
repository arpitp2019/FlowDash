package com.flowdash.dto;

import com.flowdash.domain.MindVaultSprintStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MindVaultSprintRequest(
        @NotNull Long subjectId,
        @NotBlank String title,
        String description,
        MindVaultSprintStatus status,
        LocalDate startDate,
        LocalDate dueDate,
        @Min(1) Integer estimatedSessions,
        @Min(0) Integer completedSessions
) {
}
