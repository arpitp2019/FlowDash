package com.flowdash.dto;

public record GoalStatsResponse(
        long totalGoals,
        long activeGoals,
        long completedGoals,
        long progressedToday,
        long overdueGoals,
        int todayProgress,
        int weeklyConsistency,
        int monthlyConsistency,
        int yearlyActiveDays,
        int currentStreak,
        int bestStreak
) {
}
