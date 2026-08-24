package com.anicictina.backend.quiz.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizGenerateRequest {

    @NotNull(message = "Material is required")
    private Long materialId;

    private Integer questionCount;
}
