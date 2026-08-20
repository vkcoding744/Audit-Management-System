package com.auditplatform.document.storage;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3ObjectStorageTest {

    @Test
    void putAndOpenUseBucketAndSafeKey() throws Exception {
        S3Client client = mock(S3Client.class);
        when(client.putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());
        byte[] payload = "s3-bytes".getBytes(StandardCharsets.UTF_8);
        when(client.getObject(any(GetObjectRequest.class))).thenReturn(
                new ResponseInputStream<>(
                        GetObjectResponse.builder().build(),
                        AbortableInputStream.create(new ByteArrayInputStream(payload))
                )
        );

        S3ObjectStorage storage = new S3ObjectStorage(client, "audit-docs");
        String key = StorageKeys.forDocument("tenant-a", "obj-1");
        storage.put(key, payload, "text/plain");
        try (var in = storage.open(key)) {
            assertThat(in.readAllBytes()).isEqualTo(payload);
        }
        verify(client).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
    }
}
