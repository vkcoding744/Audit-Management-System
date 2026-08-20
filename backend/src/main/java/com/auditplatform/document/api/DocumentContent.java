package com.auditplatform.document.api;

import java.io.InputStream;

public record DocumentContent(
        String filename,
        String contentType,
        long sizeBytes,
        InputStream inputStream
) {
}
