package com.anicictina.backend.ai;

import java.time.LocalDate;
import java.time.LocalTime;

public record ValidatedStudyPlanItem(
    Long subjectId,
    LocalDate date,
    LocalTime startTime,
    int durationMinutes,
    String topic
) {
}
