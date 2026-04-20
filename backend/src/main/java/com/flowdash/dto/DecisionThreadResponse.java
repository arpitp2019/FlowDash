package com.flowdash.dto;

import com.flowdash.domain.DecisionStatus;

import java.time.Instant;

public record DecisionThreadResponse(
        Long id,
        String title,
        String summary,
        String providerKey,
        String providerModel,
        DecisionStatus status,
        Instant memoGeneratedAt,
        Instant lastActiveAt,
        Instant createdAt,
        Instant updatedAt
) {
}
