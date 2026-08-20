package com.auditplatform.audit.api;

public record UpdateExecutionRequest(
        String openingNotes,
        String closingNotes
) {
}
