package com.flowdash.dto;

import java.time.LocalDate;

public record MindVaultStatsResponse(
        long totalSubjects,
        long activeSubjects,
        long totalSprints,
        long activeSprints,
        long totalItems,
        long importantItems,
        long randomItems,
        long resourceCount,
        long dueToday,
        long overdue,
        long mastered,
        long learnedThisWeek,
        long reviewsThisWeek,
        int averageMastery,
        int studyStreak,
        LocalDate nextDeadline,
        boolean fileUploadsEnabled
) {
}
