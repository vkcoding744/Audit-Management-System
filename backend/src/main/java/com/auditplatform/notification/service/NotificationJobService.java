package com.auditplatform.notification.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.tenant.TenantContext;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.notification.api.CreateJobRequest;
import com.auditplatform.notification.api.NotificationJobResponse;
import com.auditplatform.notification.domain.NotificationChannelType;
import com.auditplatform.notification.domain.NotificationJob;
import com.auditplatform.notification.domain.NotificationJobStatus;
import com.auditplatform.notification.domain.NotificationTemplate;
import com.auditplatform.notification.domain.TemplateStatus;
import com.auditplatform.notification.email.OutboundEmailPort;
import com.auditplatform.notification.repository.NotificationJobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

@Service
public class NotificationJobService {

    private final NotificationJobRepository jobRepository;
    private final NotificationNumberService numberService;
    private final NotificationTemplateService templateService;
    private final NotificationChannelService channelService;
    private final OutboundEmailPort outboundEmailPort;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public NotificationJobService(
            NotificationJobRepository jobRepository,
            NotificationNumberService numberService,
            NotificationTemplateService templateService,
            NotificationChannelService channelService,
            OutboundEmailPort outboundEmailPort,
            IsolationService isolationService,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.jobRepository = jobRepository;
        this.numberService = numberService;
        this.templateService = templateService;
        this.channelService = channelService;
        this.outboundEmailPort = outboundEmailPort;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationJobResponse> list(NotificationJobStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<NotificationJob> page = status == null
                ? jobRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                : jobRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public NotificationJobResponse get(String id) {
        return toResponse(requireJob(id));
    }

    @Transactional
    public NotificationJobResponse create(CreateJobRequest request) {
        String tenantId = isolationService.requireTenantScope();
        channelService.ensureDefaults(tenantId);
        NotificationJob job = new NotificationJob();
        job.setTenantId(tenantId);
        job.setJobNumber(numberService.nextJob(tenantId));
        job.setToAddress(request.toAddress().trim());
        job.setScheduledFor(request.scheduledFor());
        job.setStatus(NotificationJobStatus.QUEUED);
        if (request.templateId() != null && !request.templateId().isBlank()) {
            NotificationTemplate template = templateService.requireTemplate(request.templateId());
            if (template.getStatus() != TemplateStatus.ACTIVE) {
                throw new ApiException(ErrorCode.SYS_VALIDATION, "Inactive templates cannot create jobs");
            }
            Map<String, String> variables = request.variables() == null ? Map.of() : request.variables();
            job.setTemplateId(template.getId());
            job.setChannel(template.getChannel());
            job.setSubject(TemplateRenderer.render(template.getSubject(), variables));
            job.setBody(TemplateRenderer.render(template.getBody(), variables));
        } else {
            if (request.subject() == null || request.subject().isBlank() || request.body() == null || request.body().isBlank()) {
                throw new ApiException(ErrorCode.SYS_VALIDATION, "Ad-hoc jobs require subject and body");
            }
            job.setChannel(request.channel() == null ? NotificationChannelType.EMAIL : request.channel());
            job.setSubject(request.subject().trim());
            job.setBody(request.body());
        }
        jobRepository.save(job);
        auditLogService.record("NOTIFICATION_JOB_CREATE", "NotificationJob", job.getId(), null, job.getJobNumber(), null, null);
        return toResponse(job);
    }

    @Transactional
    public NotificationJobResponse send(String id) {
        return deliverLoaded(requireJob(id));
    }

    /**
     * Delivers a queued job without a security principal. Used by the scheduler and tenant dispatch.
     * Callers must already have selected a job that is allowed for the current tenant (or the system worker).
     */
    @Transactional
    public NotificationJobResponse deliverQueuedJob(String id) {
        NotificationJob job = jobRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Notification job not found"));
        return deliverLoaded(job);
    }

    private NotificationJobResponse deliverLoaded(NotificationJob job) {
        if (job.getStatus() != NotificationJobStatus.QUEUED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only queued jobs can be sent");
        }
        channelService.requireEnabled(job.getTenantId(), job.getChannel());
        String previousTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(job.getTenantId());
            try {
                if (job.getChannel() == NotificationChannelType.EMAIL) {
                    outboundEmailPort.send(job.getToAddress(), job.getSubject(), job.getBody());
                }
                job.setStatus(NotificationJobStatus.SENT);
                job.setSentAt(clock.instant());
                job.setErrorMessage(null);
                auditLogService.record("NOTIFICATION_JOB_SEND", "NotificationJob", job.getId(), "QUEUED", "SENT", null, null);
            } catch (RuntimeException ex) {
                job.setStatus(NotificationJobStatus.FAILED);
                String message = ex.getMessage() == null ? "Send failed" : ex.getMessage();
                job.setErrorMessage(message.length() > 512 ? message.substring(0, 512) : message);
            }
            jobRepository.save(job);
            return toResponse(job);
        } finally {
            TenantContext.setTenantId(previousTenant);
        }
    }

    @Transactional
    public NotificationJobResponse cancel(String id) {
        NotificationJob job = requireJob(id);
        if (job.getStatus() != NotificationJobStatus.QUEUED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only queued jobs can be cancelled");
        }
        job.setStatus(NotificationJobStatus.CANCELLED);
        jobRepository.save(job);
        auditLogService.record("NOTIFICATION_JOB_CANCEL", "NotificationJob", job.getId(), "QUEUED", "CANCELLED", null, null);
        return toResponse(job);
    }

    public NotificationJob requireJob(String id) {
        NotificationJob job = jobRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Notification job not found"));
        isolationService.assertCanAccessTenant(job.getTenantId());
        return job;
    }

    private NotificationJobResponse toResponse(NotificationJob job) {
        boolean due = job.getStatus() == NotificationJobStatus.QUEUED
                && job.getScheduledFor() != null
                && !job.getScheduledFor().isAfter(clock.instant());
        return NotificationJobResponse.from(job, due);
    }
}
