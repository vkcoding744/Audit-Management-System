package com.auditplatform.ai.api;

import com.auditplatform.ai.domain.AiGeneration;
import com.auditplatform.ai.domain.AiGenerationStatus;
import com.auditplatform.ai.domain.AiLinkedType;
import com.auditplatform.ai.domain.AiPurpose;

import java.time.Instant;

public record AiGenerationResponse(
        String id,
        String tenantId,
        String generationNumber,
        AiPurpose purpose,
        String prompt,
        String output,
        String provider,
        String model,
        String promptVersion,
        AiLinkedType linkedType,
        String linkedId,
        AiGenerationStatus status,
        String errorMessage,
        String reviewedBy,
        Instant reviewedAt,
        String reviewNotes
) {
    public static AiGenerationResponse from(AiGeneration generation) {
        return new AiGenerationResponse(
                generation.getId(),
                generation.getTenantId(),
                generation.getGenerationNumber(),
                generation.getPurpose(),
                generation.getPrompt(),
                generation.getOutput(),
                generation.getProvider(),
                generation.getModel(),
                generation.getPromptVersion(),
                generation.getLinkedType(),
                generation.getLinkedId(),
                generation.getStatus(),
                generation.getErrorMessage(),
                generation.getReviewedBy(),
                generation.getReviewedAt(),
                generation.getReviewNotes()
        );
    }
}
