package com.auditplatform.ai.spi;

import com.auditplatform.ai.config.AiProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StubAiGenerationAdapterTest {

    @Test
    void draftRequiresHumanReviewAndDoesNotCloseFindings() {
        StubAiGenerationAdapter adapter = new StubAiGenerationAdapter(new AiProperties("stub", "stub-v1", "v1"));
        AiCompletion completion = adapter.complete(new AiPrompt("FINDING_SUMMARY", "Major NC on calibration", "v1"));
        assertThat(completion.provider()).isEqualTo("stub");
        assertThat(completion.output()).contains(StubAiGenerationAdapter.HUMAN_REVIEW_NOTICE);
        assertThat(completion.output()).contains("must not be used to issue a certificate or close a finding");
        assertThat(completion.output()).contains("Major NC on calibration");
    }
}
