package com.flowdash.dto;

import java.util.List;

public record MindVaultOverviewResponse(
        MindVaultStatsResponse stats,
        MindVaultAnalyticsResponse analytics,
        List<MindVaultSubjectResponse> subjects,
        List<MindVaultSprintResponse> sprints,
        List<MindVaultItemResponse> items,
        List<MindVaultItemResponse> queue,
        List<MindVaultReviewLogResponse> recentReviews
) {
}
