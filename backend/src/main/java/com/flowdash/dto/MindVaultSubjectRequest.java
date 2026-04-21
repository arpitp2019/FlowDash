package com.flowdash.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record MindVaultSubjectRequest(
        @NotBlank String title,
        String description,
        @Min(1) @Max(5) Integer priority,
        @Min(1) @Max(100) Integer targetMastery,
        LocalDate deadline,
        String tags,
        Boolean archived
) {
}
