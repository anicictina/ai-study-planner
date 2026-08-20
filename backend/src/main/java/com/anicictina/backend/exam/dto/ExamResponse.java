package com.anicictina.backend.exam.dto;

import com.anicictina.backend.exam.Exam;
import com.anicictina.backend.exam.ExamStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import lombok.Builder;

@Builder
public record ExamResponse(
    Long id,
    Long subjectId,
    String subjectName,
    String subjectColor,
    LocalDate examDate,
    LocalTime examTime,
    String location,
    ExamStatus status,
    long daysRemaining
) {

    public static ExamResponse from(Exam exam) {
        return ExamResponse.builder()
            .id(exam.getId())
            .subjectId(exam.getSubject().getId())
            .subjectName(exam.getSubject().getName())
            .subjectColor(exam.getSubject().getColor())
            .examDate(exam.getExamDate())
            .examTime(exam.getExamTime())
            .location(exam.getLocation())
            .status(exam.getStatus())
            .daysRemaining(ChronoUnit.DAYS.between(LocalDate.now(), exam.getExamDate()))
            .build();
    }
}
