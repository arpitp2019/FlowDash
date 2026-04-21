package com.flowdash.dto;

import java.time.Instant;

public record MindVaultReviewLogResponse(
        Long id,
        Long itemId,
        String itemTitle,
        String subjectTitle,
        int rating,
        Integer previousIntervalDays,
        Integer nextIntervalDays,
        Integer masteryAfter,
        Double easeFactorAfter,
        String note,
        Instant reviewedAt
) {
}
