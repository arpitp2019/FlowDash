package com.flowdash.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank String displayName,
        @NotBlank @Size(min = 8, max = 200) String password
) {
}
