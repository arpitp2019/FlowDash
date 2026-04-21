package com.flowdash.dto;

import java.util.List;

public record MindVaultAnalyticsResponse(
        List<MindVaultSubjectAnalyticsResponse> subjects,
        List<MindVaultForecastPointResponse> forecast
) {
}
