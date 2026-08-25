package com.anicictina.backend.ai;

import java.util.List;

public record RawMaterialSummary(
    String summaryText,
    List<String> keyTerms,
    List<RawKeyDefinition> keyDefinitions,
    List<String> practiceQuestions
) {
}
