package com.flowdash.dto;

import com.flowdash.domain.MindVaultItemSource;
import com.flowdash.domain.MindVaultItemStatus;
import com.flowdash.domain.MindVaultLearningType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record MindVaultItemRequest(
        Long subjectId,
        Long sprintId,
        MindVaultItemSource source,
        MindVaultLearningType learningType,
        @NotBlank String title,
        String prompt,
        String answer,
        String notes,
        String tags,
        @Min(1) @Max(5) Integer priority,
        @Min(1) @Max(5) Integer importance,
        @Min(1) @Max(5) Integer difficulty,
        Boolean reviewEnabled,
        String sourceLabel,
        LocalDate dueDate,
        MindVaultItemStatus status
) {
}
