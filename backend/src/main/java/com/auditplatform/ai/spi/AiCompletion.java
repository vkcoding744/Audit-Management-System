package com.auditplatform.ai.spi;

public record AiCompletion(
        String provider,
        String model,
        String output
) {
}
