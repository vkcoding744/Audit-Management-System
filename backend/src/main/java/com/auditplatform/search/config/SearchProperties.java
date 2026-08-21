package com.auditplatform.search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "audit.search")
public record SearchProperties(String provider, String elasticsearchUri, int perType) {

    public String providerOrMysql() {
        return provider == null || provider.isBlank() ? "mysql" : provider.trim().toLowerCase();
    }

    public int perTypeOrDefault() {
        return perType <= 0 ? 5 : Math.min(perType, 20);
    }
}
