package com.anicictina.backend.quiz.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record QuestionResult(
    Long questionId,
    String questionText,
    List<String> options,
    Integer selectedIndex,
    int correctAnswerIndex,
    boolean correct,
    String explanation
) {
}
