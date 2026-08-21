package com.auditplatform.reporting.service;

import com.auditplatform.reporting.domain.ReportFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReportRenderer {

    private final ObjectMapper objectMapper;

    public ReportRenderer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public byte[] render(ReportFormat format, List<String> columns, List<Map<String, String>> rows) {
        return switch (format) {
            case CSV -> csv(columns, rows).getBytes(StandardCharsets.UTF_8);
            case JSON -> json(columns, rows);
        };
    }

    String csv(List<String> columns, List<Map<String, String>> rows) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.join(",", columns.stream().map(ReportRenderer::csvEscape).toList()));
        builder.append('\n');
        for (Map<String, String> row : rows) {
            List<String> values = columns.stream()
                    .map(column -> csvEscape(row.getOrDefault(column, "")))
                    .toList();
            builder.append(String.join(",", values));
            builder.append('\n');
        }
        return builder.toString();
    }

    private byte[] json(List<String> columns, List<Map<String, String>> rows) {
        List<Map<String, String>> ordered = rows.stream().map(row -> {
            Map<String, String> copy = new LinkedHashMap<>();
            for (String column : columns) {
                copy.put(column, row.getOrDefault(column, ""));
            }
            return copy;
        }).toList();
        try {
            return objectMapper.writeValueAsBytes(ordered);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not render JSON report", ex);
        }
    }

    static String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        boolean quote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        if (!quote) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
