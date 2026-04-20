package com.flowdash.dto;

public record AiProviderResponse(
        String providerKey,
        String providerLabel,
        String modelName,
        String baseUrl,
        boolean enabled,
        boolean defaultSelected,
        String source
) {
}
