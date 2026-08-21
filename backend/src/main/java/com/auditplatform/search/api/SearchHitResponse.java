package com.auditplatform.search.api;

public record SearchHitResponse(String type, String id, String title, String subtitle, String path) {
}
