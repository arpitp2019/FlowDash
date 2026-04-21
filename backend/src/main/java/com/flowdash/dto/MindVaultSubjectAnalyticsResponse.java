package com.flowdash.dto;

import java.time.LocalDate;

public record MindVaultSubjectAnalyticsResponse(
        Long subjectId,
        String title,
        long itemCount,
        long masteredCount,
        long dueCount,
        int averageMastery,
        Integer targetMastery,
        LocalDate deadline
) {
}
