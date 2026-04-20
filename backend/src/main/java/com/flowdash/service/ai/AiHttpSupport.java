package com.flowdash.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public abstract class AiHttpSupport {

    protected final ObjectMapper objectMapper;
    protected final HttpClient httpClient;

    protected AiHttpSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    protected String postJson(String url, String body, String apiKey, HeaderConfigurer headerConfigurer) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json");
            if (apiKey != null && !apiKey.isBlank()) {
                headerConfigurer.apply(builder, apiKey);
            }
            HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }
            throw new IllegalStateException("AI provider request failed with HTTP " + response.statusCode() + ": " + response.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to call AI provider", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to call AI provider", ex);
        }
    }

    protected String extractText(JsonNode node, String... paths) {
        for (String path : paths) {
            JsonNode current = node;
            String[] tokens = path.split("\\.");
            boolean found = true;
            for (String token : tokens) {
                if (token.isBlank()) {
                    continue;
                }
                if (current == null) {
                    found = false;
                    break;
                }
                current = pathToken(current, token);
                if (current.isMissingNode()) {
                    found = false;
                    break;
                }
            }
            if (found && current != null && !current.isMissingNode() && !current.isNull()) {
                String text = current.asText();
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        }
        return "";
    }

    private static JsonNode pathToken(JsonNode current, String token) {
        if (current.isArray() && token.chars().allMatch(Character::isDigit)) {
            try {
                return current.path(Integer.parseInt(token));
            } catch (NumberFormatException ignored) {
                return current.path(token);
            }
        }
        return current.path(token);
    }

    @FunctionalInterface
    protected interface HeaderConfigurer {
        void apply(HttpRequest.Builder builder, String apiKey);
    }
}
