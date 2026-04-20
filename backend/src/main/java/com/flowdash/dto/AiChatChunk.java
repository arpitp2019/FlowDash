package com.flowdash.dto;

public record AiChatChunk(
        String type,
        String content,
        boolean done
) {
}
