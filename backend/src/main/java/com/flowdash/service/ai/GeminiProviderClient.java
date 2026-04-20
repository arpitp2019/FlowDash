package com.flowdash.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flowdash.domain.AiProviderKey;
import org.springframework.stereotype.Component;

@Component
public class GeminiProviderClient extends AiHttpSupport implements AiProviderClient {

    public GeminiProviderClient(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public boolean supports(AiProviderKey providerKey) {
        return providerKey == AiProviderKey.GEMINI;
    }

    @Override
    public AiGenerationResult generate(AiGenerationRequest request) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            ArrayNode contents = root.putArray("contents");
            ObjectNode content = contents.addObject();
            ArrayNode parts = content.putArray("parts");
            if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
                parts.addObject().put("text", request.systemPrompt() + "\n\n" + request.userPrompt());
            } else {
                parts.addObject().put("text", request.userPrompt());
            }
            if (request.temperature() != null) {
                ObjectNode generationConfig = root.putObject("generationConfig");
                generationConfig.put("temperature", request.temperature().doubleValue());
            }
            String baseUrl = request.baseUrl() == null || request.baseUrl().isBlank()
                    ? "https://generativelanguage.googleapis.com/v1beta"
                    : request.baseUrl().trim();
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            String model = request.modelName() == null || request.modelName().isBlank() ? "gemini-1.5-flash" : request.modelName();
            String url = baseUrl + "/models/" + model + ":generateContent?key=" + request.apiKey();
            String response = postJson(url, objectMapper.writeValueAsString(root), request.apiKey(), (builder, apiKey) -> {
                // Gemini uses key query parameters, so no auth header is needed.
            });
            JsonNode node = objectMapper.readTree(response);
            String text = extractText(node, "candidates.0.content.parts.0.text");
            return new AiGenerationResult("gemini", model, text);
        } catch (Exception ex) {
            throw new IllegalStateException("Gemini request failed", ex);
        }
    }
}
