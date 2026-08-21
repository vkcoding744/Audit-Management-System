package com.auditplatform.ai.spi;

public record AiPrompt(
        String purpose,
        String input,
        String promptVersion
) {
}
