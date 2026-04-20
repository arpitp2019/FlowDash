package com.flowdash.service.ai;

import com.flowdash.domain.AiProviderKey;

public interface AiProviderClient {
    boolean supports(AiProviderKey providerKey);

    AiGenerationResult generate(AiGenerationRequest request);
}
