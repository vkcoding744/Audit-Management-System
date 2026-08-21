package com.auditplatform.ai.service;

import com.auditplatform.ai.api.AiGenerationResponse;
import com.auditplatform.ai.api.CreateAiGenerationRequest;
import com.auditplatform.ai.api.ReviewAiGenerationRequest;
import com.auditplatform.ai.api.UpdateAiGenerationRequest;
import com.auditplatform.ai.config.AiProperties;
import com.auditplatform.ai.domain.AiGeneration;
import com.auditplatform.ai.domain.AiGenerationStatus;
import com.auditplatform.ai.domain.AiPurpose;
import com.auditplatform.ai.repository.AiGenerationRepository;
import com.auditplatform.ai.spi.AiCompletion;
import com.auditplatform.ai.spi.AiGenerationPort;
import com.auditplatform.ai.spi.AiPrompt;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class AiGenerationService {

    private final AiGenerationRepository generationRepository;
    private final AiNumberService numberService;
    private final AiGenerationPort generationPort;
    private final AiProperties properties;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public AiGenerationService(
            AiGenerationRepository generationRepository,
            AiNumberService numberService,
            AiGenerationPort generationPort,
            AiProperties properties,
            IsolationService isolationService,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.generationRepository = generationRepository;
        this.numberService = numberService;
        this.generationPort = generationPort;
        this.properties = properties;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<AiGenerationResponse> list(AiGenerationStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<AiGeneration> page = status == null
                ? generationRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                : generationRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        return PageResponse.from(page.map(AiGenerationResponse::from));
    }

    @Transactional(readOnly = true)
    public AiGenerationResponse get(String id) {
        return AiGenerationResponse.from(requireGeneration(id));
    }

    @Transactional
    public AiGenerationResponse create(CreateAiGenerationRequest request) {
        if (request.linkedType() != null && (request.linkedId() == null || request.linkedId().isBlank())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "linkedId is required when linkedType is set");
        }
        String tenantId = isolationService.requireTenantScope();
        AiGeneration generation = new AiGeneration();
        generation.setTenantId(tenantId);
        generation.setGenerationNumber(numberService.nextGeneration(tenantId));
        generation.setPurpose(request.purpose() == null ? AiPurpose.GENERIC : request.purpose());
        generation.setPrompt(request.prompt());
        generation.setLinkedType(request.linkedType());
        generation.setLinkedId(blankToNull(request.linkedId()));
        generation.setPromptVersion(properties.promptVersionOrDefault());
        generation.setProvider(properties.providerOrDefault());
        generation.setModel(properties.modelOrDefault());
        generation.setOutput("");
        try {
            AiCompletion completion = generationPort.complete(new AiPrompt(
                    generation.getPurpose().name(),
                    generation.getPrompt(),
                    generation.getPromptVersion()
            ));
            generation.setProvider(completion.provider());
            generation.setModel(completion.model());
            generation.setOutput(completion.output());
            generation.setStatus(AiGenerationStatus.PENDING_REVIEW);
        } catch (RuntimeException ex) {
            generation.setStatus(AiGenerationStatus.FAILED);
            generation.setOutput("");
            generation.setErrorMessage(truncate(ex.getMessage()));
        }
        generationRepository.save(generation);
        auditLogService.record("AI_GENERATE", "AiGeneration", generation.getId(), null, generation.getGenerationNumber(), null, null);
        return AiGenerationResponse.from(generation);
    }

    @Transactional
    public AiGenerationResponse update(String id, UpdateAiGenerationRequest request) {
        AiGeneration generation = requirePending(id);
        if (request.output() != null && !request.output().isBlank()) {
            generation.setOutput(request.output());
        }
        if (request.reviewNotes() != null) {
            generation.setReviewNotes(blankToNull(request.reviewNotes()));
        }
        generationRepository.save(generation);
        return AiGenerationResponse.from(generation);
    }

    @Transactional
    public AiGenerationResponse approve(String id, ReviewAiGenerationRequest request) {
        AiGeneration generation = requirePending(id);
        generation.setStatus(AiGenerationStatus.APPROVED);
        generation.setReviewedBy(isolationService.requirePrincipal().userId());
        generation.setReviewedAt(Instant.now(clock));
        if (request != null && request.notes() != null) {
            generation.setReviewNotes(blankToNull(request.notes()));
        }
        generationRepository.save(generation);
        auditLogService.record("AI_APPROVE", "AiGeneration", generation.getId(), null, generation.getGenerationNumber(), null, null);
        return AiGenerationResponse.from(generation);
    }

    @Transactional
    public AiGenerationResponse reject(String id, ReviewAiGenerationRequest request) {
        AiGeneration generation = requirePending(id);
        generation.setStatus(AiGenerationStatus.REJECTED);
        generation.setReviewedBy(isolationService.requirePrincipal().userId());
        generation.setReviewedAt(Instant.now(clock));
        if (request != null && request.notes() != null) {
            generation.setReviewNotes(blankToNull(request.notes()));
        }
        generationRepository.save(generation);
        auditLogService.record("AI_REJECT", "AiGeneration", generation.getId(), null, generation.getGenerationNumber(), null, null);
        return AiGenerationResponse.from(generation);
    }

    public AiGeneration requireGeneration(String id) {
        AiGeneration generation = generationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "AI generation not found"));
        isolationService.assertCanAccessTenant(generation.getTenantId());
        return generation;
    }

    private AiGeneration requirePending(String id) {
        AiGeneration generation = requireGeneration(id);
        if (generation.getStatus() != AiGenerationStatus.PENDING_REVIEW) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only generations pending review can be changed");
        }
        return generation;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String truncate(String message) {
        if (message == null || message.isBlank()) {
            return "Generation failed";
        }
        return message.length() <= 512 ? message : message.substring(0, 512);
    }
}
