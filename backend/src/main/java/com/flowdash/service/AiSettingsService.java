package com.flowdash.service;

import com.flowdash.domain.AiProviderKey;
import com.flowdash.domain.AiProviderSetting;
import com.flowdash.domain.AppUser;
import com.flowdash.repository.AiProviderSettingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AiSettingsService {

    private final AiProviderSettingRepository settingRepository;

    @Value("${FLOWDASH_AI_PROVIDER:mock}")
    private String defaultProvider;

    @Value("${FLOWDASH_AI_MODEL:flowdash-mock}")
    private String defaultModel;

    @Value("${FLOWDASH_AI_BASE_URL:}")
    private String defaultBaseUrl;

    public AiSettingsService(AiProviderSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public AiProviderSetting resolveFor(AppUser user) {
        if (user != null) {
            return settingRepository.findFirstByUserAndDefaultSelectedTrue(user)
                    .orElseGet(() -> buildDefaultForUser(user));
        }
        return settingRepository.findFirstByUserIsNullAndDefaultSelectedTrue()
                .orElseGet(this::buildGlobalDefault);
    }

    public String resolveApiKey(AiProviderKey providerKey) {
        return switch (providerKey) {
            case GEMINI -> envOrDefault("GEMINI_API_KEY", "");
            case CLAUDE -> envOrDefault("ANTHROPIC_API_KEY", "");
            case MINIMAX -> envOrDefault("MINIMAX_API_KEY", "");
            case GLM -> envOrDefault("GLM_API_KEY", "");
            case DEEPSEEK -> envOrDefault("DEEPSEEK_API_KEY", "");
            case OPENAI -> envOrDefault("OPENAI_API_KEY", "");
            case GROK -> envOrDefault("XAI_API_KEY", envOrDefault("GROK_API_KEY", ""));
            case MOCK -> "";
        };
    }

    private AiProviderSetting buildDefaultForUser(AppUser user) {
        return new AiProviderSetting(user, parseProvider(defaultProvider), providerLabel(defaultProvider), defaultModel, defaultBaseUrl, true, true, new BigDecimal("0.30"));
    }

    private AiProviderSetting buildGlobalDefault() {
        return new AiProviderSetting(null, parseProvider(defaultProvider), providerLabel(defaultProvider), defaultModel, defaultBaseUrl, true, true, new BigDecimal("0.30"));
    }

    private static AiProviderKey parseProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            return AiProviderKey.MOCK;
        }
        try {
            return AiProviderKey.valueOf(provider.trim().toUpperCase());
        } catch (Exception ignored) {
            return AiProviderKey.MOCK;
        }
    }

    private static String providerLabel(String provider) {
        if (provider == null || provider.isBlank()) {
            return "Mock";
        }
        return provider.trim();
    }

    private static String envOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
