package com.anicictina.backend.statistics.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record WeeklyStudyResponse(
    LocalDate weekStart,
    int totalMinutes,
    List<DayMinutes> byDay,
    List<SubjectMinutes> bySubject
) {
}
