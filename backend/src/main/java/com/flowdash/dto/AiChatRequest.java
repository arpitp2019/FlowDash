package com.flowdash.dto;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        Long threadId,
        @NotBlank String tabKey,
        @NotBlank String prompt,
        String context
) {
}
