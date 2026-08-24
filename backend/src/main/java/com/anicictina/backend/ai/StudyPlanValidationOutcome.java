package com.anicictina.backend.ai;

import java.util.List;

public record StudyPlanValidationOutcome(
    List<ValidatedStudyPlanItem> validItems,
    List<RejectedStudyPlanItem> rejectedItems
) {
}
