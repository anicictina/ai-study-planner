package com.anicictina.backend.material.dto;

import com.anicictina.backend.material.MaterialStatus;
import com.anicictina.backend.material.StudyMaterial;
import java.time.Instant;
import lombok.Builder;

@Builder
public record MaterialResponse(
    Long id,
    Long subjectId,
    String subjectName,
    String title,
    String content,
    MaterialStatus status,
    Instant createdAt
) {

    public static MaterialResponse from(StudyMaterial material) {
        return MaterialResponse.builder()
            .id(material.getId())
            .subjectId(material.getSubject().getId())
            .subjectName(material.getSubject().getName())
            .title(material.getTitle())
            .content(material.getContent())
            .status(material.getStatus())
            .createdAt(material.getCreatedAt())
            .build();
    }

    public static MaterialResponse summaryFrom(StudyMaterial material) {
        String preview = material.getContent().length() > 200
            ? material.getContent().substring(0, 200) + "..."
            : material.getContent();

        return MaterialResponse.builder()
            .id(material.getId())
            .subjectId(material.getSubject().getId())
            .subjectName(material.getSubject().getName())
            .title(material.getTitle())
            .content(preview)
            .status(material.getStatus())
            .createdAt(material.getCreatedAt())
            .build();
    }
}
