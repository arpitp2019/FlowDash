package com.flowdash.dto;

import com.flowdash.domain.MindVaultResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MindVaultResourceRequest(
        @NotNull MindVaultResourceType resourceType,
        @NotBlank String title,
        String description,
        String url
) {
}
