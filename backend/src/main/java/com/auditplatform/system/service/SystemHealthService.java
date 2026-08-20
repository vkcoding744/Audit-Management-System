package com.auditplatform.system.service;

import com.auditplatform.system.api.SystemHealthResponse;
import com.auditplatform.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;

@Service
public class SystemHealthService {

    private final DataSource dataSource;
    private final TenantRepository tenantRepository;

    public SystemHealthService(DataSource dataSource, TenantRepository tenantRepository) {
        this.dataSource = dataSource;
        this.tenantRepository = tenantRepository;
    }

    @Transactional(readOnly = true)
    public SystemHealthResponse health() {
        boolean dbUp = isDatabaseUp();
        long tenants = dbUp ? tenantRepository.countByDeletedAtIsNull() : -1L;
        return new SystemHealthResponse(
                dbUp ? "UP" : "DOWN",
                dbUp ? "UP" : "DOWN",
                tenants
        );
    }

    public boolean isDatabaseUp() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception ex) {
            return false;
        }
    }
}
