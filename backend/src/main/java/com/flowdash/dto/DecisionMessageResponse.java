package com.flowdash.dto;

import java.time.Instant;

public record DecisionMessageResponse(
        Long id,
        Long threadId,
        String role,
        String tabKey,
        String content,
        String model,
        Instant createdAt
) {
}
