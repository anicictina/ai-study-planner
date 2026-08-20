package com.anicictina.backend.studysession.dto;

import com.anicictina.backend.studysession.ActivityType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudySessionRequest {

    @NotNull(message = "Subject is required")
    private Long subjectId;

    private String topic;

    @NotNull(message = "Session date is required")
    private LocalDate sessionDate;

    private LocalTime startTime;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    private ActivityType activityType;
}
