package com.anicictina.backend.studyplan.dto;

import com.anicictina.backend.studyplan.StudyPlan;
import com.anicictina.backend.studyplan.StudyPlanStatus;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record StudyPlanResponse(
    Long id,
    Instant generatedAt,
    StudyPlanStatus status,
    int rejectedItemsCount,
    String validationNotes,
    List<StudyPlanItemResponse> items
) {

    public static StudyPlanResponse from(StudyPlan plan) {
        return StudyPlanResponse.builder()
            .id(plan.getId())
            .generatedAt(plan.getGeneratedAt())
            .status(plan.getStatus())
            .rejectedItemsCount(plan.getRejectedItemsCount())
            .validationNotes(plan.getValidationNotes())
            .items(plan.getItems().stream().map(StudyPlanItemResponse::from).toList())
            .build();
    }
}
