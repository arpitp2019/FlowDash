package com.flowdash.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record MindVaultReviewRequest(
        @Min(0) @Max(3) int rating,
        String note
) {
}
