package com.auditplatform.governance.api;

import com.auditplatform.governance.domain.RiskCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRiskRequest(
        @NotBlank @Size(max = 255) String title,
        RiskCategory category,
        @Min(1) @Max(5) Integer likelihood,
        @Min(1) @Max(5) Integer impact,
        String description
) {
}
