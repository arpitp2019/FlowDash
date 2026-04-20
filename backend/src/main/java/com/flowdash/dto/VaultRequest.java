package com.flowdash.dto;

import com.flowdash.domain.VaultEntryType;
import jakarta.validation.constraints.NotBlank;

public record VaultRequest(
        @NotBlank String title,
        @NotBlank String content,
        VaultEntryType entryType,
        String tags,
        Boolean favorite
) {
}
