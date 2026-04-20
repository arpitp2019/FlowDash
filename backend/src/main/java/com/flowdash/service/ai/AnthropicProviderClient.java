package com.flowdash.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flowdash.domain.AiProviderKey;
import org.springframework.stereotype.Component;

@Component
public class AnthropicProviderClient extends AiHttpSupport implements AiProviderClient {

    public AnthropicProviderClient(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public boolean supports(AiProviderKey providerKey) {
        return providerKey == AiProviderKey.CLAUDE;
    }

    @Override
    public AiGenerationResult generate(AiGenerationRequest request) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", request.modelName());
            root.put("max_tokens", 1200);
            ArrayNode messages = root.putArray("messages");
            if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
                messages.addObject().put("role", "user").put("content", "[System]\n" + request.systemPrompt());
            }
            messages.addObject().put("role", "user").put("content", request.userPrompt());
            String url = normalizeBaseUrl(request.baseUrl(), "/v1/messages");
            String response = postJson(url, objectMapper.writeValueAsString(root), request.apiKey(), (builder, apiKey) -> {
                builder.header("x-api-key", apiKey);
                builder.header("anthropic-version", "2023-06-01");
            });
            JsonNode node = objectMapper.readTree(response);
            String text = extractText(node, "content.0.text", "content.0.input_text");
            return new AiGenerationResult("claude", request.modelName(), text);
        } catch (Exception ex) {
            throw new IllegalStateException("Anthropic request failed", ex);
        }
    }

    private static String normalizeBaseUrl(String baseUrl, String path) {
        String cleaned = baseUrl == null || baseUrl.isBlank() ? "https://api.anthropic.com" : baseUrl.trim();
        if (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        return cleaned + path;
    }
}
