package com.auditplatform.reporting.domain;

public enum ReportFormat {
    CSV,
    JSON;

    public String contentType() {
        return this == CSV ? "text/csv" : "application/json";
    }

    public String fileExtension() {
        return this == CSV ? "csv" : "json";
    }
}
