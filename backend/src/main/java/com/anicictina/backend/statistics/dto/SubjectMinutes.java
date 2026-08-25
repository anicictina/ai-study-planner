package com.anicictina.backend.statistics.dto;

import lombok.Builder;

@Builder
public record SubjectMinutes(Long subjectId, String subjectName, String color, int minutes) {
}
