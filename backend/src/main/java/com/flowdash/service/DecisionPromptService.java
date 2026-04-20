package com.flowdash.service;

import com.flowdash.domain.DecisionFramework;
import org.springframework.stereotype.Service;

@Service
public class DecisionPromptService {

    public String systemPromptFor(String tabKey) {
        DecisionFramework framework = parse(tabKey);
        return switch (framework) {
            case FIRST_PRINCIPLES -> """
                    You are a decision coach using first principles thinking.
                    Break the problem into facts, assumptions, constraints, and the smallest irreducible truths.
                    Prefer clarity over opinion. Present a practical path forward.
                    """;
            case INVERSION -> """
                    You are a decision coach using inversion thinking.
                    Start by asking how this decision could fail, what to avoid, and what mistakes would make the outcome worse.
                    Then invert those risks into safeguards.
                    """;
            case SECOND_ORDER -> """
                    You are a decision coach using second-order thinking.
                    Explain the immediate effect, the next effect, and the hidden downstream effect.
                    Highlight tradeoffs, unintended consequences, and compounding impacts.
                    """;
            case BOOK_MODELS -> """
                    You are a decision coach inspired by The Decision Book.
                    Recommend the most suitable model among Eisenhower, Rubber Band, Yes/No Rule, Choice Overload, Consequences, Stop Rule, Hard Choice, Bias Check, Crossroads, Rumsfeld Matrix, Black Swan, Prisoner's Dilemma, and Pareto.
                    Explain why the model fits and how to use it.
                    """;
            case SYNTHESIS -> """
                    You are a decision coach producing a final recommendation.
                    Synthesize the previous analysis into a clear recommendation, tradeoffs, risks, and the next action.
                    Keep it decisive and practical.
                    """;
        };
    }

    public String summaryPrompt(String userPrompt, String threadSummary, String tabKey) {
        String prompt = """
                Decision topic:
                %s

                Current thread context:
                %s

                Framework tab:
                %s

                Respond with a crisp but useful analysis.
                """.formatted(
                userPrompt,
                threadSummary == null || threadSummary.isBlank() ? "No prior context yet." : threadSummary,
                parse(tabKey).name()
        );
        return prompt;
    }

    public DecisionFramework parse(String tabKey) {
        if (tabKey == null) {
            return DecisionFramework.SYNTHESIS;
        }
        String normalized = tabKey.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        return switch (normalized) {
            case "FIRST", "FIRST_PRINCIPLES" -> DecisionFramework.FIRST_PRINCIPLES;
            case "INVERSION" -> DecisionFramework.INVERSION;
            case "SECOND_ORDER", "SECONDORDER", "SECOND" -> DecisionFramework.SECOND_ORDER;
            case "BOOK_MODELS", "MODEL", "MODELS" -> DecisionFramework.BOOK_MODELS;
            default -> DecisionFramework.SYNTHESIS;
        };
    }
}
