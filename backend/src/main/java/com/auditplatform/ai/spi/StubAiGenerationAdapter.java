package com.auditplatform.ai.spi;

import com.auditplatform.ai.config.AiProperties;

public class StubAiGenerationAdapter implements AiGenerationPort {

    public static final String HUMAN_REVIEW_NOTICE =
            "Human review required. This draft must not be used to issue a certificate or close a finding.";

    private final AiProperties properties;

    public StubAiGenerationAdapter(AiProperties properties) {
        this.properties = properties;
    }

    @Override
    public AiCompletion complete(AiPrompt prompt) {
        String purpose = prompt.purpose() == null || prompt.purpose().isBlank() ? "GENERIC" : prompt.purpose();
        String input = prompt.input() == null ? "" : prompt.input();
        String output = HUMAN_REVIEW_NOTICE
                + "\nProvider: stub"
                + "\nPurpose: " + purpose
                + "\nPrompt version: " + prompt.promptVersion()
                + "\n\nDraft:\n"
                + input;
        return new AiCompletion(properties.providerOrDefault(), properties.modelOrDefault(), output);
    }
}
