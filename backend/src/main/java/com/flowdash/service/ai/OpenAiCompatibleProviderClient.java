package com.flowdash.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flowdash.domain.AiProviderKey;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class OpenAiCompatibleProviderClient extends AiHttpSupport implements AiProviderClient {

    private static final Set<AiProviderKey> SUPPORTED = Set.of(
            AiProviderKey.OPENAI,
            AiProviderKey.DEEPSEEK,
            AiProviderKey.MINIMAX,
            AiProviderKey.GLM,
            AiProviderKey.GROK
    );

    public OpenAiCompatibleProviderClient(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public boolean supports(AiProviderKey providerKey) {
        return SUPPORTED.contains(providerKey);
    }

    @Override
    public AiGenerationResult generate(AiGenerationRequest request) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", request.modelName());
            root.put("temperature", request.temperature() == null ? 0.3 : request.temperature().doubleValue());
            ArrayNode messages = root.putArray("messages");
            if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
                messages.addObject().put("role", "system").put("content", request.systemPrompt());
            }
            List<AiMessage> requestMessages = request.messages();
            if (requestMessages != null && !requestMessages.isEmpty()) {
                for (AiMessage message : requestMessages) {
                    messages.addObject().put("role", message.role()).put("content", message.content());
                }
            }
            messages.addObject().put("role", "user").put("content", request.userPrompt());
            String url = normalizeBaseUrl(request.baseUrl(), "/chat/completions");
            String response = postJson(url, objectMapper.writeValueAsString(root), request.apiKey(), (builder, apiKey) -> {
                builder.header("Authorization", "Bearer " + apiKey);
            });
            JsonNode node = objectMapper.readTree(response);
            String text = extractText(node, "choices.0.message.content", "choices.0.delta.content");
            return new AiGenerationResult(request.providerKey().name().toLowerCase(), request.modelName(), text);
        } catch (Exception ex) {
            throw new IllegalStateException("OpenAI-compatible request failed", ex);
        }
    }

    private static String normalizeBaseUrl(String baseUrl, String path) {
        String cleaned = baseUrl == null || baseUrl.isBlank() ? "https://api.openai.com/v1" : baseUrl.trim();
        if (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        if (cleaned.endsWith("/v1") && path.startsWith("/")) {
            return cleaned + path;
        }
        return cleaned + path;
    }
}
