package com.auditplatform.crm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "crm_sequences")
@IdClass(CrmSequenceId.class)
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
}
