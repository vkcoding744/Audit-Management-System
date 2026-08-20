package com.auditplatform.document.service;

import com.auditplatform.audit.service.AuditService;
import com.auditplatform.audit.service.FindingService;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.certification.service.CertificateService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.crm.service.ClientService;
import com.auditplatform.document.repository.DocumentRepository;
import com.auditplatform.document.storage.ObjectStoragePort;
import com.auditplatform.identity.service.IsolationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class DocumentServiceTest {

    private final IsolationService isolationService = new IsolationService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void uploadRejectsDisallowedContentType() {
        bindUser();
        DocumentService service = new DocumentService(
                mock(DocumentRepository.class),
                mock(DocumentNumberService.class),
                mock(ObjectStoragePort.class),
                mock(ClientService.class),
                mock(AuditService.class),
                mock(FindingService.class),
                mock(CertificateService.class),
                isolationService,
                mock(AuditLogService.class)
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "payload.exe",
                "application/x-msdownload",
                new byte[] {0x4d, 0x5a}
        );

        assertThatThrownBy(() -> service.upload(file, "malware", null, null, null, null, null))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getErrorCode()).isEqualTo(ErrorCode.SYS_VALIDATION);
                    assertThat(api.getMessage()).contains("not allowed");
                });
    }

    private void bindUser() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("DOCUMENT_UPLOAD")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
