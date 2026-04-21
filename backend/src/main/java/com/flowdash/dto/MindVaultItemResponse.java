package com.flowdash.dto;

import com.flowdash.domain.MindVaultItemSource;
import com.flowdash.domain.MindVaultItemStatus;

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
        MindVaultItemStatus status,
        String title,
        String prompt,
        String answer,
        String notes,
        List<String> tags,
        Integer priority,
        Integer difficulty,
        Integer masteryScore,
        Integer reviewStreak,
        Integer reviewCount,
        Integer successCount,
        Double easeFactor,
        Integer reviewIntervalDays,
        LocalDate nextReviewDate,
        LocalDate dueDate,
        Instant lastReviewedAt,
        Integer lastRating,
        boolean mastered,
        boolean dueToday,
        boolean overdue,
        String queueReason,
        Instant createdAt,
        Instant updatedAt
) {
}
