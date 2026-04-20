package com.flowdash.dto;

import com.flowdash.domain.VaultEntryType;

import java.time.Instant;

public record VaultResponse(
        Long id,
        String title,
        String content,
        VaultEntryType entryType,
        String tags,
        boolean favorite,
        Instant createdAt,
        Instant updatedAt
) {
}
