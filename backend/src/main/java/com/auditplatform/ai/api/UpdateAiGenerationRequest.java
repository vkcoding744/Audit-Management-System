package com.auditplatform.ai.api;

import jakarta.validation.constraints.Size;

public record UpdateAiGenerationRequest(
        String output,
        @Size(max = 512) String reviewNotes
) {
}
