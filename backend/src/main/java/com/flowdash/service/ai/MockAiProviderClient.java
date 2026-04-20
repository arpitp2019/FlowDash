package com.flowdash.service.ai;

import com.flowdash.domain.AiProviderKey;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class MockAiProviderClient implements AiProviderClient {

    @Override
    public boolean supports(AiProviderKey providerKey) {
        return providerKey == AiProviderKey.MOCK;
    }

    @Override
    public AiGenerationResult generate(AiGenerationRequest request) {
        String text = """
                I reviewed the decision through the %s lens.

                Key question: %s

                Practical next step: write down the best option, the main risk, and the smallest test you can run this week.
                """.formatted(
                request.systemPrompt(),
                request.userPrompt()
        );
        if (request.messages() != null && !request.messages().isEmpty()) {
            String recent = request.messages().stream().map(message -> message.role() + ": " + message.content()).collect(Collectors.joining("\n"));
            text = text + "\nContext seen by the backend:\n" + recent;
        }
        return new AiGenerationResult("mock", request.modelName(), text);
    }
}
