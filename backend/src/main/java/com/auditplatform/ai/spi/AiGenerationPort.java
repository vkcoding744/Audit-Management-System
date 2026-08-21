package com.auditplatform.ai.spi;

public interface AiGenerationPort {

    AiCompletion complete(AiPrompt prompt);
}
