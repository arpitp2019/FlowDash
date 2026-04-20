package com.flowdash.service.ai;

import com.flowdash.domain.AiProviderKey;
import com.flowdash.domain.AiProviderSetting;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiProviderRegistry {

    private final List<AiProviderClient> clients;

    public AiProviderRegistry(List<AiProviderClient> clients) {
        this.clients = clients;
    }

    public AiProviderClient resolve(AiProviderKey providerKey) {
        return clients.stream()
                .filter(client -> client.supports(providerKey))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No AI provider registered for " + providerKey));
    }

    public AiProviderKey sanitizeProvider(AiProviderKey providerKey) {
        return providerKey == null ? AiProviderKey.MOCK : providerKey;
    }

    public AiGenerationResult generate(AiProviderSetting setting, AiGenerationRequest request) {
        AiProviderKey providerKey = sanitizeProvider(setting == null ? null : setting.getProviderKey());
        AiProviderClient client = resolve(providerKey);
        return client.generate(request);
    }
}
