package com.flowdash.dto;

import jakarta.validation.constraints.NotBlank;

public record DecisionMessageRequest(
        @NotBlank String content,
        @NotBlank String tabKey
) {
}
