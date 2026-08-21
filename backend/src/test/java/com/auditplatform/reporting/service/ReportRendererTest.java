package com.auditplatform.reporting.service;

import com.auditplatform.reporting.domain.ReportFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReportRendererTest {

    private final ReportRenderer renderer = new ReportRenderer(new ObjectMapper());

    @Test
    void csvQuotesFieldsThatContainCommas() {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("legalName", "Acme, Inc.");
        row.put("status", "ACTIVE");
        String csv = renderer.csv(List.of("legalName", "status"), List.of(row));
        assertThat(csv).isEqualTo("legalName,status\n\"Acme, Inc.\",ACTIVE\n");
    }

    @Test
    void jsonRendersArrayOfRowObjects() {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("legalName", "Acme");
        row.put("status", "ACTIVE");
        String json = new String(
                renderer.render(ReportFormat.JSON, List.of("legalName", "status"), List.of(row)),
                StandardCharsets.UTF_8
        );
        assertThat(json).contains("\"legalName\":\"Acme\"");
        assertThat(json).contains("\"status\":\"ACTIVE\"");
    }
}
