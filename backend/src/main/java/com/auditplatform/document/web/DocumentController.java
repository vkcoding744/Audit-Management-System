package com.auditplatform.document.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.document.api.DocumentContent;
import com.auditplatform.document.api.DocumentResponse;
import com.auditplatform.document.domain.DocumentCategory;
import com.auditplatform.document.domain.DocumentLinkType;
import com.auditplatform.document.service.DocumentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.MDC;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/documents")
    @PreAuthorize("hasAuthority('DOCUMENT_VIEW')")
    public ApiResponse<PageResponse<DocumentResponse>> list(
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) DocumentLinkType linkedType,
            @RequestParam(required = false) String linkedId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(documentService.list(clientId, linkedType, linkedId, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DOCUMENT_UPLOAD')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DocumentResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) DocumentLinkType linkedType,
            @RequestParam(required = false) String linkedId,
            @RequestParam(required = false) DocumentCategory category,
            @RequestParam(required = false) String notes
    ) {
        return ApiResponse.ok(
                documentService.upload(file, title, clientId, linkedType, linkedId, category, notes),
                MDC.get(CorrelationId.MDC_KEY)
        );
    }

    @GetMapping("/documents/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_VIEW')")
    public ApiResponse<DocumentResponse> get(@PathVariable String id) {
        return ApiResponse.ok(documentService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/documents/{id}/content")
    @PreAuthorize("hasAuthority('DOCUMENT_DOWNLOAD')")
    public ResponseEntity<InputStreamResource> download(@PathVariable String id) {
        DocumentContent content = documentService.download(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(content.filename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentLength(content.sizeBytes())
                .body(new InputStreamResource(content.inputStream()));
    }

    @DeleteMapping("/documents/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        documentService.delete(id);
    }
}
