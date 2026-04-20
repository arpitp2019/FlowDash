package com.flowdash.service.ai;

import com.flowdash.domain.AiProviderKey;

import java.math.BigDecimal;
import java.util.List;

public record AiGenerationRequest(
        AiProviderKey providerKey,
        String modelName,
        String baseUrl,
        String apiKey,
        BigDecimal temperature,
        String systemPrompt,
        String userPrompt,
        List<AiMessage> messages
) {
}
