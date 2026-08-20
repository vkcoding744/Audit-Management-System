package com.auditplatform.tenant.domain;

import com.auditplatform.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "platform_settings")
@Getter
@Setter
public class PlatformSetting extends AuditableEntity {

    @Column(name = "tenant_id", length = 36)
    private String tenantId;

    @Column(name = "setting_key", nullable = false, length = 128)
    private String settingKey;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;
}
