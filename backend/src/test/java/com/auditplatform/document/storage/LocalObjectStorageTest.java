package com.auditplatform.document.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalObjectStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void putAndOpenRoundTrip() throws Exception {
        LocalObjectStorage storage = new LocalObjectStorage(tempDir);
        String key = StorageKeys.forDocument("tenant-a", "obj-1");
        storage.put(key, "evidence".getBytes(StandardCharsets.UTF_8), "text/plain");
        try (var in = storage.open(key)) {
            assertThat(in.readAllBytes()).isEqualTo("evidence".getBytes(StandardCharsets.UTF_8));
        }
        storage.delete(key);
        assertThatThrownBy(() -> storage.open(key)).isInstanceOf(ObjectStorageException.class);
    }

    @Test
    void rejectsPathTraversalKeys() {
        LocalObjectStorage storage = new LocalObjectStorage(tempDir);
        assertThatThrownBy(() -> storage.put("../etc/passwd", new byte[] {1}, "text/plain"))
                .isInstanceOf(ObjectStorageException.class);
    }
}
