package com.auditplatform.document.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "audit.storage")
public record StorageProperties(
        String provider,
        String localRoot,
        String s3Bucket,
        String s3Region,
        String s3Endpoint
) {
    public Path localRootPath() {
        String root = localRoot == null || localRoot.isBlank()
                ? Path.of(System.getProperty("java.io.tmpdir"), "audit-platform-files").toString()
                : localRoot;
        return Path.of(root);
    }

    public String regionOrDefault() {
        if (s3Region == null || s3Region.isBlank()) {
            return "us-east-1";
        }
        return s3Region;
    }
}
