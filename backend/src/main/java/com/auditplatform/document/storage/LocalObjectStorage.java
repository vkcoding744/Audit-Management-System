package com.auditplatform.document.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalObjectStorage implements ObjectStoragePort {

    private final Path root;

    public LocalObjectStorage(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    @Override
    public void put(String key, byte[] content, String contentType) {
        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException ex) {
            throw new ObjectStorageException("Could not write object", ex);
        }
    }

    @Override
    public InputStream open(String key) {
        Path target = resolve(key);
        try {
            return Files.newInputStream(target);
        } catch (IOException ex) {
            throw new ObjectStorageException("Could not read object", ex);
        }
    }

    @Override
    public void delete(String key) {
        Path target = resolve(key);
        try {
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            throw new ObjectStorageException("Could not delete object", ex);
        }
    }

    private Path resolve(String key) {
        Path target = root.resolve(StorageKeys.requireSafe(key)).normalize();
        if (!target.startsWith(root)) {
            throw new ObjectStorageException("Storage key escapes the configured root");
        }
        return target;
    }
}
