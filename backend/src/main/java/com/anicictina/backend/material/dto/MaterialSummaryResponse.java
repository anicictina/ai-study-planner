package com.anicictina.backend.material.dto;

import com.anicictina.backend.material.MaterialSummary;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record MaterialSummaryResponse(
    Long id,
    Long materialId,
    String summaryText,
    List<String> keyTerms,
    List<KeyDefinitionResponse> keyDefinitions,
    List<String> practiceQuestions,
    Instant generatedAt
) {

    public static MaterialSummaryResponse from(MaterialSummary summary) {
        return MaterialSummaryResponse.builder()
            .id(summary.getId())
            .materialId(summary.getMaterial().getId())
            .summaryText(summary.getSummaryText())
            .keyTerms(summary.getKeyTerms())
            .keyDefinitions(summary.getKeyDefinitions().stream()
                .map(def -> new KeyDefinitionResponse(def.getTerm(), def.getDefinition()))
                .toList())
            .practiceQuestions(summary.getPracticeQuestions())
            .generatedAt(summary.getGeneratedAt())
            .build();
    }

    public record KeyDefinitionResponse(String term, String definition) {
    }
}
