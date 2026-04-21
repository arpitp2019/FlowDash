package com.flowdash.dto;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

public record MindVaultSubjectResponse(
        Long id,
        String title,
        String description,
        Integer priority,
        Integer targetMastery,
        LocalDate deadline,
        List<String> tags,
        boolean archived,
        long itemCount,
        long masteredCount,
        long dueCount,
        int averageMastery,
        Instant createdAt,
        Instant updatedAt
) {
}
