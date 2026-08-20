package com.auditplatform.document.storage;

import com.auditplatform.document.config.StorageProperties;
import jakarta.annotation.PreDestroy;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;
import java.net.URI;

public class S3ObjectStorage implements ObjectStoragePort {

    private final S3Client client;
    private final String bucket;

    public S3ObjectStorage(StorageProperties properties) {
        if (properties.s3Bucket() == null || properties.s3Bucket().isBlank()) {
            throw new IllegalStateException("audit.storage.s3-bucket is required when provider=s3");
        }
        this.bucket = properties.s3Bucket();
        S3ClientBuilder builder = S3Client.builder().region(Region.of(properties.regionOrDefault()));
        if (properties.s3Endpoint() != null && !properties.s3Endpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.s3Endpoint()))
                    .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }
        this.client = builder.build();
    }

    S3ObjectStorage(S3Client client, String bucket) {
        this.client = client;
        this.bucket = bucket;
    }

    @Override
    public void put(String key, byte[] content, String contentType) {
        String safe = StorageKeys.requireSafe(key);
        try {
            client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(safe)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(content)
            );
        } catch (S3Exception ex) {
            throw new ObjectStorageException("Could not write object", ex);
        }
    }

    @Override
    public InputStream open(String key) {
        String safe = StorageKeys.requireSafe(key);
        try {
            return client.getObject(GetObjectRequest.builder().bucket(bucket).key(safe).build());
        } catch (S3Exception ex) {
            throw new ObjectStorageException("Could not read object", ex);
        }
    }

    @Override
    public void delete(String key) {
        String safe = StorageKeys.requireSafe(key);
        try {
            client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(safe).build());
        } catch (S3Exception ex) {
            throw new ObjectStorageException("Could not delete object", ex);
        }
    }

    @PreDestroy
    public void close() {
        client.close();
    }
}
