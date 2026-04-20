package com.flowdash.dto;

import jakarta.validation.constraints.NotBlank;

public record DecisionThreadRequest(
        @NotBlank String title
) {
}
