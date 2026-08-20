package com.auditplatform.document.config;

import com.auditplatform.document.storage.LocalObjectStorage;
import com.auditplatform.document.storage.ObjectStoragePort;
import com.auditplatform.document.storage.S3ObjectStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfiguration {

    @Bean
    @ConditionalOnProperty(name = "audit.storage.provider", havingValue = "local", matchIfMissing = true)
    public ObjectStoragePort localObjectStorage(StorageProperties properties) {
        return new LocalObjectStorage(properties.localRootPath());
    }

    @Bean
    @ConditionalOnProperty(name = "audit.storage.provider", havingValue = "s3")
    public ObjectStoragePort s3ObjectStorage(StorageProperties properties) {
        return new S3ObjectStorage(properties);
    }
}
