package com.flowdash.dto;

import java.util.List;

public record GoalOverviewResponse(
        GoalStatsResponse stats,
        GoalAnalyticsResponse analytics,
        List<GoalOverviewItemResponse> goals,
        List<GoalOverviewItemResponse> checklist,
        List<GoalDayResponse> calendarDays
) {
}
