package com.auditplatform.reporting.domain;

import com.auditplatform.audit.domain.AuditStatus;
import com.auditplatform.audit.domain.FindingStatus;
import com.auditplatform.certification.domain.CertificateStatus;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.crm.domain.ClientStatus;
import com.auditplatform.finance.domain.InvoiceStatus;
import com.auditplatform.governance.domain.ComplaintStatus;

public enum ReportDataset {
    CLIENTS(ClientStatus.class),
    AUDITS(AuditStatus.class),
    FINDINGS(FindingStatus.class),
    CERTIFICATES(CertificateStatus.class),
    INVOICES(InvoiceStatus.class),
    COMPLAINTS(ComplaintStatus.class);

    private final Class<? extends Enum<?>> statusType;

    ReportDataset(Class<? extends Enum<?>> statusType) {
        this.statusType = statusType;
    }

    public void validateStatusFilter(String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return;
        }
        String candidate = statusFilter.trim();
        for (Enum<?> constant : statusType.getEnumConstants()) {
            if (constant.name().equals(candidate)) {
                return;
            }
        }
        throw new ApiException(ErrorCode.SYS_VALIDATION, "statusFilter is not valid for dataset " + name());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E extends Enum<E>> E parseStatus(String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return null;
        }
        validateStatusFilter(statusFilter);
        return (E) Enum.valueOf((Class) statusType, statusFilter.trim());
    }
}
