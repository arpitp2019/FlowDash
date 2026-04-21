package com.flowdash.dto;

import java.time.LocalDate;

public record MindVaultStatsResponse(
        long totalSubjects,
        long activeSubjects,
        long totalSprints,
        long activeSprints,
        long totalItems,
        long randomItems,
        long dueToday,
        long overdue,
        long mastered,
        long reviewsThisWeek,
        int averageMastery,
        int studyStreak,
        LocalDate nextDeadline
) {
}
