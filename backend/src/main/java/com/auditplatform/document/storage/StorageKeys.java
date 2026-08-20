package com.auditplatform.document.storage;

import java.util.UUID;

public final class StorageKeys {

    private StorageKeys() {
    }

    public static String forDocument(String tenantId, String objectId) {
        return requireSafe(tenantId + "/" + objectId);
    }

    public static String newObjectId() {
        return UUID.randomUUID().toString();
    }

    public static String requireSafe(String key) {
        if (key == null || key.isBlank() || key.contains("..") || key.startsWith("/") || !key.matches("[a-zA-Z0-9._/-]+")) {
            throw new ObjectStorageException("Invalid storage key");
        }
        return key;
    }
}
