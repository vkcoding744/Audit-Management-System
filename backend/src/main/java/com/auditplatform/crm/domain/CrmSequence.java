package com.auditplatform.crm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "crm_sequences")
@IdClass(CrmSequence.Id.class)
@Getter
@Setter
public class CrmSequence {

    @Id
    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @Id
    @Column(name = "sequence_name", length = 64, nullable = false)
    private String sequenceName;

    @Column(name = "next_value", nullable = false)
    private long nextValue;

    public static final class Id implements Serializable {
        private String tenantId;
        private String sequenceName;

        public Id() {
        }

        public Id(String tenantId, String sequenceName) {
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
            if (!(o instanceof Id other)) {
                return false;
            }
            return Objects.equals(tenantId, other.tenantId) && Objects.equals(sequenceName, other.sequenceName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tenantId, sequenceName);
        }
    }
}
