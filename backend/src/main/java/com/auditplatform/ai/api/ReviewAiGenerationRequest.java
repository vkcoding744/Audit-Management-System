package com.auditplatform.ai.api;

import jakarta.validation.constraints.Size;

public record ReviewAiGenerationRequest(
        @Size(max = 512) String notes
) {
}
