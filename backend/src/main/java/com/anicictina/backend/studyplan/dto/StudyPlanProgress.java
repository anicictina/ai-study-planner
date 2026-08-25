package com.anicictina.backend.studyplan.dto;

import lombok.Builder;

@Builder
public record StudyPlanProgress(
    int totalItems,
    int completedItems,
    int overdueItems
) {
}
