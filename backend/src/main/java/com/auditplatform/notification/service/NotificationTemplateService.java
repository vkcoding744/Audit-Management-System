package com.auditplatform.notification.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.notification.api.CreateTemplateRequest;
import com.auditplatform.notification.api.TemplateResponse;
import com.auditplatform.notification.api.UpdateTemplateRequest;
import com.auditplatform.notification.domain.NotificationChannelType;
import com.auditplatform.notification.domain.NotificationEventType;
import com.auditplatform.notification.domain.NotificationTemplate;
import com.auditplatform.notification.domain.TemplateStatus;
import com.auditplatform.notification.repository.NotificationTemplateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;

    public NotificationTemplateService(
            NotificationTemplateRepository templateRepository,
            IsolationService isolationService,
            AuditLogService auditLogService
    ) {
        this.templateRepository = templateRepository;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<TemplateResponse> list(TemplateStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<NotificationTemplate> page = status == null
                ? templateRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                : templateRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        return PageResponse.from(page.map(TemplateResponse::from));
    }

    @Transactional(readOnly = true)
    public TemplateResponse get(String id) {
        return TemplateResponse.from(requireTemplate(id));
    }

    @Transactional
    public TemplateResponse create(CreateTemplateRequest request) {
        String tenantId = isolationService.requireTenantScope();
        String code = request.code().trim().toUpperCase();
        if (templateRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code).isPresent()) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "A template with this code already exists");
        }
        NotificationTemplate template = new NotificationTemplate();
        template.setTenantId(tenantId);
        template.setCode(code);
        template.setName(request.name().trim());
        template.setChannel(request.channel() == null ? NotificationChannelType.EMAIL : request.channel());
        template.setEventType(request.eventType() == null ? NotificationEventType.GENERIC : request.eventType());
        template.setSubject(request.subject().trim());
        template.setBody(request.body());
        template.setStatus(TemplateStatus.ACTIVE);
        templateRepository.save(template);
        auditLogService.record("NOTIFICATION_TEMPLATE_CREATE", "NotificationTemplate", template.getId(), null, template.getCode(), null, null);
        return TemplateResponse.from(template);
    }

    @Transactional
    public TemplateResponse update(String id, UpdateTemplateRequest request) {
        NotificationTemplate template = requireTemplate(id);
        if (request.name() != null && !request.name().isBlank()) {
            template.setName(request.name().trim());
        }
        if (request.channel() != null) {
            template.setChannel(request.channel());
        }
        if (request.eventType() != null) {
            template.setEventType(request.eventType());
        }
        if (request.subject() != null && !request.subject().isBlank()) {
            template.setSubject(request.subject().trim());
        }
        if (request.body() != null && !request.body().isBlank()) {
            template.setBody(request.body());
        }
        templateRepository.save(template);
        return TemplateResponse.from(template);
    }

    @Transactional
    public TemplateResponse activate(String id) {
        NotificationTemplate template = requireTemplate(id);
        template.setStatus(TemplateStatus.ACTIVE);
        templateRepository.save(template);
        return TemplateResponse.from(template);
    }

    @Transactional
    public TemplateResponse deactivate(String id) {
        NotificationTemplate template = requireTemplate(id);
        template.setStatus(TemplateStatus.INACTIVE);
        templateRepository.save(template);
        return TemplateResponse.from(template);
    }

    public NotificationTemplate requireTemplate(String id) {
        NotificationTemplate template = templateRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Notification template not found"));
        isolationService.assertCanAccessTenant(template.getTenantId());
        return template;
    }
}
