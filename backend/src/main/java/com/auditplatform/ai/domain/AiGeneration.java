package com.auditplatform.ai.domain;

import com.auditplatform.common.persistence.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "ai_generations")
@Getter
@Setter
public class AiGeneration extends TenantAwareEntity {

    @Column(name = "generation_number", nullable = false, length = 32)
    private String generationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 64)
    private AiPurpose purpose = AiPurpose.GENERIC;

    @Column(name = "prompt", nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "output", nullable = false, columnDefinition = "TEXT")
    private String output;

    @Column(name = "provider", nullable = false, length = 64)
    private String provider;

    @Column(name = "model", nullable = false, length = 128)
    private String model;

    @Column(name = "prompt_version", nullable = false, length = 32)
    private String promptVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "linked_type", length = 32)
    private AiLinkedType linkedType;

    @Column(name = "linked_id", length = 36)
    private String linkedId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AiGenerationStatus status = AiGenerationStatus.PENDING_REVIEW;

    @Column(name = "error_message", length = 512)
    private String errorMessage;

    @Column(name = "reviewed_by", length = 64)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "review_notes", length = 512)
    private String reviewNotes;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
