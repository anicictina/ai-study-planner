package com.anicictina.backend.statistics.dto;

import lombok.Builder;

@Builder
public record MaterialProgressResponse(
    Long subjectId,
    String subjectName,
    String color,
    int totalMaterials,
    int learnedMaterials,
    int learnedPercent
) {
}
