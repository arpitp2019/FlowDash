package com.flowdash.dto;

import com.flowdash.domain.MindVaultItemSource;
import com.flowdash.domain.MindVaultItemStatus;
import com.flowdash.domain.MindVaultLearningType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record MindVaultItemResponse(
        Long id,
        Long subjectId,
        String subjectTitle,
        Long sprintId,
        String sprintTitle,
        MindVaultItemSource source,
        MindVaultLearningType learningType,
        MindVaultItemStatus status,
        String title,
        String prompt,
        String answer,
        String notes,
        List<String> tags,
        Integer priority,
        Integer importance,
        Integer difficulty,
        Integer masteryScore,
        Integer reviewStreak,
        Integer reviewCount,
        Integer successCount,
        Integer lapseCount,
        Double easeFactor,
        Integer reviewIntervalDays,
        LocalDate nextReviewDate,
        LocalDate dueDate,
        Instant lastReviewedAt,
        Integer lastRating,
        boolean reviewEnabled,
        String sourceLabel,
        List<MindVaultResourceResponse> resources,
        boolean mastered,
        boolean dueToday,
        boolean overdue,
        String queueReason,
        Instant createdAt,
        Instant updatedAt
) {
}
