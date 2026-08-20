package com.auditplatform.standards.api;

import com.auditplatform.standards.domain.Standard;
import com.auditplatform.standards.domain.StandardStatus;

import java.time.Instant;

public record StandardResponse(
        String id,
        String tenantId,
        String code,
        String name,
        String publisher,
        String edition,
        String description,
        StandardStatus status,
        Instant publishedAt,
        String notes
) {
    public static StandardResponse from(Standard standard) {
        return new StandardResponse(
                standard.getId(),
                standard.getTenantId(),
                standard.getCode(),
                standard.getName(),
                standard.getPublisher(),
                standard.getEdition(),
                standard.getDescription(),
                standard.getStatus(),
                standard.getPublishedAt(),
                standard.getNotes()
        );
    }
}
