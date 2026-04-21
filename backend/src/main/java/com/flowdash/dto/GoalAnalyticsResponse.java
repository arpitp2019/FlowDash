package com.flowdash.dto;

import java.util.List;
import java.util.Map;

public record GoalAnalyticsResponse(
        Map<String, Long> statusMix,
        Map<String, Long> priorityMix,
        List<GoalOverviewItemResponse> overdueGoals,
        List<GoalOverviewItemResponse> topStreaks
) {
}
