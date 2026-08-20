package com.auditplatform.document.service;

import com.auditplatform.audit.service.AuditService;
import com.auditplatform.audit.service.FindingService;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.certification.service.CertificateService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.crm.service.ClientService;
import com.auditplatform.document.domain.Document;
import com.auditplatform.document.repository.DocumentRepository;
import com.auditplatform.document.storage.ObjectStoragePort;
import com.auditplatform.identity.service.IsolationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DocumentServiceIsolationTest {

    private final IsolationService isolationService = new IsolationService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getDoesNotReturnAnotherTenantsDocument() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("DOCUMENT_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        Document foreign = new Document();
        foreign.setTenantId("tenant-b");
        DocumentRepository documents = mock(DocumentRepository.class);
        when(documents.findByIdAndDeletedAtIsNull("d1")).thenReturn(Optional.of(foreign));

        DocumentService service = new DocumentService(
                documents,
                mock(DocumentNumberService.class),
                mock(ObjectStoragePort.class),
                mock(ClientService.class),
                mock(AuditService.class),
                mock(FindingService.class),
                mock(CertificateService.class),
                isolationService,
                mock(AuditLogService.class)
        );

        assertThatThrownBy(() -> service.get("d1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_TENANT_MISMATCH);
    }
}
