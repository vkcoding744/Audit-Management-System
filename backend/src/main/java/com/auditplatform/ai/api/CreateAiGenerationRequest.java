package com.auditplatform.ai.api;

import com.auditplatform.ai.domain.AiLinkedType;
import com.auditplatform.ai.domain.AiPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAiGenerationRequest(
        AiPurpose purpose,
        @NotBlank String prompt,
        AiLinkedType linkedType,
        @Size(max = 36) String linkedId
) {
}
