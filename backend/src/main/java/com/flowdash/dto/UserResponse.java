package com.flowdash.dto;

public record UserResponse(Long id, String email, String displayName, String authProvider) {
}
