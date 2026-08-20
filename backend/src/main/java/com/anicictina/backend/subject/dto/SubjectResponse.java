package com.anicictina.backend.subject.dto;

import com.anicictina.backend.subject.Level;
import com.anicictina.backend.subject.Subject;
import java.time.Instant;
import lombok.Builder;

@Builder
public record SubjectResponse(
    Long id,
    String name,
    String description,
    Integer credits,
    Level difficulty,
    Level priority,
    Integer knowledgePercent,
    String color,
    boolean archived,
    Instant createdAt
) {

    public static SubjectResponse from(Subject subject) {
        return SubjectResponse.builder()
            .id(subject.getId())
            .name(subject.getName())
            .description(subject.getDescription())
            .credits(subject.getCredits())
            .difficulty(subject.getDifficulty())
            .priority(subject.getPriority())
            .knowledgePercent(subject.getKnowledgePercent())
            .color(subject.getColor())
            .archived(subject.isArchived())
            .createdAt(subject.getCreatedAt())
            .build();
    }
}
