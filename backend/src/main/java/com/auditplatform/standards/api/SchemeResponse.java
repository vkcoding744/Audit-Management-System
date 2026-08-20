package com.auditplatform.standards.api;

import com.auditplatform.standards.domain.Scheme;
import com.auditplatform.standards.domain.SchemeStatus;

import java.util.List;

public record SchemeResponse(
        String id,
        String tenantId,
        String code,
        String name,
        String description,
        String accreditationBody,
        Integer cycleMonths,
        Integer surveillanceIntervalMonths,
        SchemeStatus status,
        String notes,
        List<StandardResponse> standards
) {
    public static SchemeResponse from(Scheme scheme, List<StandardResponse> standards) {
        return new SchemeResponse(
                scheme.getId(),
                scheme.getTenantId(),
                scheme.getCode(),
                scheme.getName(),
                scheme.getDescription(),
                scheme.getAccreditationBody(),
                scheme.getCycleMonths(),
                scheme.getSurveillanceIntervalMonths(),
                scheme.getStatus(),
                scheme.getNotes(),
                standards
        );
    }
}
