package com.anicictina.backend.studysession.dto;

import com.anicictina.backend.studysession.ActivityType;
import com.anicictina.backend.studysession.StudySession;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record StudySessionResponse(
    Long id,
    Long subjectId,
    String subjectName,
    String subjectColor,
    String topic,
    LocalDate sessionDate,
    LocalTime startTime,
    Integer durationMinutes,
    boolean completed,
    ActivityType activityType
) {

    public static StudySessionResponse from(StudySession session) {
        return StudySessionResponse.builder()
            .id(session.getId())
            .subjectId(session.getSubject().getId())
            .subjectName(session.getSubject().getName())
            .subjectColor(session.getSubject().getColor())
            .topic(session.getTopic())
            .sessionDate(session.getSessionDate())
            .startTime(session.getStartTime())
            .durationMinutes(session.getDurationMinutes())
            .completed(session.isCompleted())
            .activityType(session.getActivityType())
            .build();
    }
}
