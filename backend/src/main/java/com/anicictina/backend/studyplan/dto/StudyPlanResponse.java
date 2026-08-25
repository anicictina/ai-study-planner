package com.anicictina.backend.studyplan.dto;

import com.anicictina.backend.studyplan.StudyPlan;
import com.anicictina.backend.studyplan.StudyPlanItem;
import com.anicictina.backend.studyplan.StudyPlanStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
public record StudyPlanResponse(
    Long id,
    Instant generatedAt,
    StudyPlanStatus status,
    int rejectedItemsCount,
    String validationNotes,
    List<StudyPlanItemResponse> items,
    StudyPlanProgress progress
) {

    public static StudyPlanResponse from(StudyPlan plan) {
        return StudyPlanResponse.builder()
            .id(plan.getId())
            .generatedAt(plan.getGeneratedAt())
            .status(plan.getStatus())
            .rejectedItemsCount(plan.getRejectedItemsCount())
            .validationNotes(plan.getValidationNotes())
            .items(plan.getItems().stream().map(StudyPlanItemResponse::from).toList())
            .progress(null)
            .build();
    }

    public static StudyPlanResponse fromAccepted(
        StudyPlan plan,
        Map<Long, Boolean> completedByLinkedSessionId,
        LocalDate today
    ) {
        List<StudyPlanItemResponse> items = plan.getItems().stream()
            .map(item -> StudyPlanItemResponse.from(item, resolveCompleted(item, completedByLinkedSessionId)))
            .toList();

        int totalItems = items.size();
        int completedItems = (int) items.stream().filter(i -> Boolean.TRUE.equals(i.completed())).count();
        int overdueItems = (int) plan.getItems().stream()
            .filter(item -> !Boolean.TRUE.equals(resolveCompleted(item, completedByLinkedSessionId)))
            .filter(item -> item.getItemDate().isBefore(today))
            .count();

        return StudyPlanResponse.builder()
            .id(plan.getId())
            .generatedAt(plan.getGeneratedAt())
            .status(plan.getStatus())
            .rejectedItemsCount(plan.getRejectedItemsCount())
            .validationNotes(plan.getValidationNotes())
            .items(items)
            .progress(StudyPlanProgress.builder()
                .totalItems(totalItems)
                .completedItems(completedItems)
                .overdueItems(overdueItems)
                .build())
            .build();
    }

    private static Boolean resolveCompleted(StudyPlanItem item, Map<Long, Boolean> completedByLinkedSessionId) {
        if (item.getLinkedSessionId() == null) {
            return null;
        }
        return completedByLinkedSessionId.getOrDefault(item.getLinkedSessionId(), false);
    }
}
