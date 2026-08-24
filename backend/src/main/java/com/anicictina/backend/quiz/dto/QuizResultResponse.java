package com.anicictina.backend.quiz.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record QuizResultResponse(
    Long quizId,
    int correctCount,
    int totalCount,
    List<QuestionResult> results
) {
}
