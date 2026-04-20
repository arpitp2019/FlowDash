package com.flowdash.service.ai;

public record AiGenerationResult(
        String providerKey,
        String modelName,
        String text
) {
}
