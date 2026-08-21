package com.auditplatform.search.spi;

import com.auditplatform.search.domain.SearchType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class ElasticsearchQueryBuilder {

    private ElasticsearchQueryBuilder() {
    }

    public static String build(String tenantId, String query, SearchType type, int size) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ObjectNode bool = root.putObject("query").putObject("bool");
        ArrayNode filter = bool.putArray("filter");
        filter.addObject().putObject("term").put("tenantId", tenantId);
        if (type != null) {
            filter.addObject().putObject("term").put("type", type.name());
        }
        ObjectNode multi = bool.putArray("must").addObject().putObject("multi_match");
        multi.put("query", query);
        ArrayNode fields = multi.putArray("fields");
        fields.add("title").add("subtitle").add("id");
        root.put("size", size);
        return root.toString();
    }
}
