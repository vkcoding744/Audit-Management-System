package com.auditplatform.document.service;

import com.auditplatform.audit.service.AuditService;
import com.auditplatform.audit.service.FindingService;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.certification.service.CertificateService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.crm.service.ClientService;
import com.auditplatform.document.api.DocumentContent;
import com.auditplatform.document.api.DocumentResponse;
import com.auditplatform.document.domain.Document;
import com.auditplatform.document.domain.DocumentCategory;
import com.auditplatform.document.domain.DocumentLinkType;
import com.auditplatform.document.repository.DocumentRepository;
import com.auditplatform.document.storage.ObjectStorageException;
import com.auditplatform.document.storage.ObjectStoragePort;
import com.auditplatform.document.storage.StorageKeys;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class DocumentService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "text/plain",
            "text/csv",
            "application/zip",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/msword",
            "application/vnd.ms-excel",
            "application/vnd.ms-powerpoint",
            "application/vnd.oasis.opendocument.text",
            "application/vnd.oasis.opendocument.spreadsheet"
    );

    private static final Map<String, String> TYPES_BY_EXTENSION = Map.ofEntries(
            Map.entry("pdf", "application/pdf"),
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("png", "image/png"),
            Map.entry("gif", "image/gif"),
            Map.entry("webp", "image/webp"),
            Map.entry("txt", "text/plain"),
            Map.entry("csv", "text/csv"),
            Map.entry("zip", "application/zip"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            Map.entry("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
            Map.entry("doc", "application/msword"),
            Map.entry("xls", "application/vnd.ms-excel"),
            Map.entry("ppt", "application/vnd.ms-powerpoint"),
            Map.entry("odt", "application/vnd.oasis.opendocument.text"),
            Map.entry("ods", "application/vnd.oasis.opendocument.spreadsheet")
    );

    private final DocumentRepository documentRepository;
    private final DocumentNumberService numberService;
    private final ObjectStoragePort objectStorage;
    private final ClientService clientService;
    private final AuditService auditService;
    private final FindingService findingService;
    private final CertificateService certificateService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentNumberService numberService,
            ObjectStoragePort objectStorage,
            ClientService clientService,
            AuditService auditService,
            FindingService findingService,
            CertificateService certificateService,
            IsolationService isolationService,
            AuditLogService auditLogService
    ) {
        this.documentRepository = documentRepository;
        this.numberService = numberService;
        this.objectStorage = objectStorage;
        this.clientService = clientService;
        this.auditService = auditService;
        this.findingService = findingService;
        this.certificateService = certificateService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<DocumentResponse> list(String clientId, DocumentLinkType linkedType, String linkedId, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<Document> page;
        if (linkedId != null && !linkedId.isBlank()) {
            page = documentRepository.findByTenantIdAndLinkedIdAndDeletedAtIsNull(tenantId, linkedId, pageable);
        } else if (clientId != null && !clientId.isBlank()) {
            page = documentRepository.findByTenantIdAndClientIdAndDeletedAtIsNull(tenantId, clientId, pageable);
        } else if (linkedType != null) {
            page = documentRepository.findByTenantIdAndLinkedTypeAndDeletedAtIsNull(tenantId, linkedType, pageable);
        } else {
            page = documentRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.from(page.map(DocumentResponse::from));
    }

    @Transactional(readOnly = true)
    public DocumentResponse get(String id) {
        return DocumentResponse.from(requireDocument(id));
    }

    @Transactional
    public DocumentResponse upload(
            MultipartFile file,
            String title,
            String clientId,
            DocumentLinkType linkedType,
            String linkedId,
            DocumentCategory category,
            String notes
    ) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "A non-empty file is required");
        }
        String tenantId = isolationService.requireTenantScope();
        String filename = sanitizeFilename(file.getOriginalFilename());
        String contentType = resolveContentType(file.getContentType(), filename);
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "File type is not allowed");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Could not read uploaded file");
        }
        if (bytes.length == 0) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "A non-empty file is required");
        }
        LinkTarget link = resolveLink(linkedType, linkedId, clientId);
        String objectId = StorageKeys.newObjectId();
        String storageKey = StorageKeys.forDocument(tenantId, objectId);
        try {
            objectStorage.put(storageKey, bytes, contentType);
        } catch (ObjectStorageException ex) {
            throw new ApiException(ErrorCode.SYS_INTERNAL, "Could not store document");
        }
        Document document = new Document();
        document.setTenantId(tenantId);
        document.setDocumentNumber(numberService.nextDocument(tenantId));
        document.setTitle(blankToNull(title) == null ? stripExtension(filename) : title.trim());
        document.setOriginalFilename(filename);
        document.setContentType(contentType);
        document.setSizeBytes(bytes.length);
        document.setChecksumSha256(sha256(bytes));
        document.setStorageKey(storageKey);
        document.setClientId(link.clientId());
        document.setLinkedType(link.linkedType());
        document.setLinkedId(link.linkedId());
        document.setCategory(category == null ? DocumentCategory.EVIDENCE : category);
        document.setNotes(blankToNull(notes));
        documentRepository.save(document);
        auditLogService.record(
                "DOCUMENT_UPLOAD",
                "Document",
                document.getId(),
                null,
                document.getDocumentNumber(),
                null,
                null
        );
        return DocumentResponse.from(document);
    }

    @Transactional(readOnly = true)
    public DocumentContent download(String id) {
        Document document = requireDocument(id);
        try {
            return new DocumentContent(
                    document.getOriginalFilename(),
                    document.getContentType(),
                    document.getSizeBytes(),
                    objectStorage.open(document.getStorageKey())
            );
        } catch (ObjectStorageException ex) {
            throw new ApiException(ErrorCode.SYS_INTERNAL, "Could not read document content");
        }
    }

    @Transactional
    public void delete(String id) {
        Document document = requireDocument(id);
        document.setDeletedAt(Instant.now());
        documentRepository.save(document);
        try {
            objectStorage.delete(document.getStorageKey());
        } catch (ObjectStorageException ex) {
            throw new ApiException(ErrorCode.SYS_INTERNAL, "Could not delete document content");
        }
        auditLogService.record("DOCUMENT_DELETE", "Document", document.getId(), document.getDocumentNumber(), null, null, null);
    }

    public Document requireDocument(String id) {
        Document document = documentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Document not found"));
        isolationService.assertCanAccessTenant(document.getTenantId());
        return document;
    }

    private LinkTarget resolveLink(DocumentLinkType linkedType, String linkedId, String clientId) {
        DocumentLinkType type = linkedType == null ? DocumentLinkType.GENERAL : linkedType;
        if (type == DocumentLinkType.GENERAL) {
            String resolvedClient = blankToNull(clientId);
            if (resolvedClient != null) {
                clientService.requireClient(resolvedClient);
            }
            return new LinkTarget(type, null, resolvedClient);
        }
        if (linkedId == null || linkedId.isBlank()) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "linkedId is required when linkedType is not GENERAL");
        }
        return switch (type) {
            case CLIENT -> {
                var client = clientService.requireClient(linkedId);
                yield new LinkTarget(type, client.getId(), client.getId());
            }
            case AUDIT -> {
                var audit = auditService.requireAudit(linkedId);
                yield new LinkTarget(type, audit.getId(), audit.getClientId());
            }
            case FINDING -> {
                var finding = findingService.requireFinding(linkedId);
                yield new LinkTarget(type, finding.getId(), finding.getClientId());
            }
            case CERTIFICATE -> {
                var certificate = certificateService.requireCertificate(linkedId);
                yield new LinkTarget(type, certificate.getId(), certificate.getClientId());
            }
            case GENERAL -> new LinkTarget(DocumentLinkType.GENERAL, null, blankToNull(clientId));
        };
    }

    private String resolveContentType(String declared, String filename) {
        if (declared != null && !declared.isBlank() && !"application/octet-stream".equalsIgnoreCase(declared)) {
            return declared.toLowerCase(Locale.ROOT);
        }
        String extension = extension(filename);
        String inferred = TYPES_BY_EXTENSION.get(extension);
        if (inferred == null) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "File type is not allowed");
        }
        return inferred;
    }

    private static String sanitizeFilename(String original) {
        if (original == null || original.isBlank()) {
            return "file";
        }
        String name = Path.of(original.replace('\\', '/')).getFileName().toString();
        name = name.replaceAll("[^A-Za-z0-9._-]", "_");
        if (name.isBlank() || name.equals(".") || name.equals("..")) {
            return "file";
        }
        if (name.length() > 255) {
            return name.substring(name.length() - 255);
        }
        return name;
    }

    private static String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot <= 0) {
            return filename;
        }
        return filename.substring(0, dot);
    }

    private static String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String sha256(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private record LinkTarget(DocumentLinkType linkedType, String linkedId, String clientId) {
    }
}
