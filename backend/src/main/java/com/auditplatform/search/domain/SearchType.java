package com.auditplatform.search.domain;

public enum SearchType {
    CLIENT("/clients/"),
    LEAD("/leads/"),
    AUDIT("/audits/"),
    FINDING("/findings/"),
    CERTIFICATE("/certificates/"),
    DOCUMENT("/documents/"),
    COMPLAINT("/complaints/");

    private final String pathPrefix;

    SearchType(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    public String pathFor(String id) {
        return pathPrefix + id;
    }
}
