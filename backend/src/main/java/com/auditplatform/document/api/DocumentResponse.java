package com.auditplatform.document.api;

import com.auditplatform.document.domain.Document;
import com.auditplatform.document.domain.DocumentCategory;
import com.auditplatform.document.domain.DocumentLinkType;

import java.time.Instant;

public record DocumentResponse(
        String id,
        String tenantId,
        String documentNumber,
        String title,
        String originalFilename,
        String contentType,
        long sizeBytes,
        String checksumSha256,
        String clientId,
        DocumentLinkType linkedType,
        String linkedId,
        DocumentCategory category,
        String notes,
        Instant createdAt
) {
    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getTenantId(),
                document.getDocumentNumber(),
                document.getTitle(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getChecksumSha256(),
                document.getClientId(),
                document.getLinkedType(),
                document.getLinkedId(),
                document.getCategory(),
                document.getNotes(),
                document.getCreatedAt()
        );
    }
}
