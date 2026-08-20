package com.auditplatform.tenant.repository;

import com.auditplatform.tenant.domain.PlatformSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformSettingRepository extends JpaRepository<PlatformSetting, String> {

    Optional<PlatformSetting> findByTenantIdAndSettingKey(String tenantId, String settingKey);
}
