package com.anicictina.backend.exam.dto;

import com.anicictina.backend.exam.ExamStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamRequest {

    @NotNull(message = "Subject is required")
    private Long subjectId;

    @NotNull(message = "Exam date is required")
    private LocalDate examDate;

    private LocalTime examTime;

    private String location;

    private ExamStatus status;
}
