package com.anicictina.backend.studyplan.dto;

import com.anicictina.backend.studyplan.StudyPlanItem;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record StudyPlanItemResponse(
    Long id,
    Long subjectId,
    String subjectName,
    String subjectColor,
    LocalDate itemDate,
    LocalTime startTime,
    Integer durationMinutes,
    String topic
) {

    public static StudyPlanItemResponse from(StudyPlanItem item) {
        return StudyPlanItemResponse.builder()
            .id(item.getId())
            .subjectId(item.getSubject().getId())
            .subjectName(item.getSubject().getName())
            .subjectColor(item.getSubject().getColor())
            .itemDate(item.getItemDate())
            .startTime(item.getStartTime())
            .durationMinutes(item.getDurationMinutes())
            .topic(item.getTopic())
            .build();
    }
}
