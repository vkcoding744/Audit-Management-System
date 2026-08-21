package com.auditplatform.search.spi;

public final class LikeQuery {

    private LikeQuery() {
    }

    public static String escape(String raw) {
        return raw.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
