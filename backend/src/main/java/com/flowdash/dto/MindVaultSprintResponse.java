package com.flowdash.dto;

import com.flowdash.domain.MindVaultSprintStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record MindVaultSprintResponse(
        Long id,
        Long subjectId,
        String subjectTitle,
        String title,
        String description,
        MindVaultSprintStatus status,
        LocalDate startDate,
        LocalDate dueDate,
        Integer estimatedSessions,
        Integer completedSessions,
        List<String> subjectTags,
        long itemCount,
        long masteredCount,
        long dueCount,
        int progress,
        Instant createdAt,
        Instant updatedAt
) {
}
