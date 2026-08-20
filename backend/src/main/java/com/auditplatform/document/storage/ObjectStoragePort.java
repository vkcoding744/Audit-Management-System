package com.auditplatform.document.storage;

import java.io.InputStream;

public interface ObjectStoragePort {

    void put(String key, byte[] content, String contentType);

    InputStream open(String key);

    void delete(String key);
}
