package com.auditplatform.reporting.api;

import java.io.InputStream;

public record ReportExportContent(
        String filename,
        String contentType,
        long sizeBytes,
        InputStream inputStream
) {
}
