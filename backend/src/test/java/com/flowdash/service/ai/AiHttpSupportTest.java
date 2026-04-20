package com.flowdash.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiHttpSupportTest {

    private final Support support = new Support(new ObjectMapper());

    @Test
    void extractTextHandlesArrayIndexesInProviderPaths() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        assertThat(support.extract(mapper.readTree("""
                {"choices":[{"message":{"content":"openai text"}}]}
                """), "choices.0.message.content")).isEqualTo("openai text");

        assertThat(support.extract(mapper.readTree("""
                {"content":[{"text":"claude text"}]}
                """), "content.0.text")).isEqualTo("claude text");

        assertThat(support.extract(mapper.readTree("""
                {"candidates":[{"content":{"parts":[{"text":"gemini text"}]}}]}
                """), "candidates.0.content.parts.0.text")).isEqualTo("gemini text");
    }

    private static class Support extends AiHttpSupport {
        Support(ObjectMapper objectMapper) {
            super(objectMapper);
        }

        String extract(com.fasterxml.jackson.databind.JsonNode node, String... paths) {
            return extractText(node, paths);
        }
    }
}
