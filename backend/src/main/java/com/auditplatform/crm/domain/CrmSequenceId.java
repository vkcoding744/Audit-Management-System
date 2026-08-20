package com.auditplatform.crm.domain;

import java.io.Serializable;
import java.util.Objects;

public final class CrmSequenceId implements Serializable {

    private String tenantId;
    private String sequenceName;

    public CrmSequenceId() {
    }

    public CrmSequenceId(String tenantId, String sequenceName) {
        this.tenantId = tenantId;
        this.sequenceName = sequenceName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CrmSequenceId other)) {
            return false;
        }
        return Objects.equals(tenantId, other.tenantId) && Objects.equals(sequenceName, other.sequenceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, sequenceName);
    }
}
