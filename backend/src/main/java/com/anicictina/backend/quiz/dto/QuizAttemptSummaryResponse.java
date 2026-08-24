package com.anicictina.backend.quiz.dto;

import com.anicictina.backend.quiz.QuizAttempt;
import java.time.Instant;
import lombok.Builder;

@Builder
public record QuizAttemptSummaryResponse(
    Long id,
    Long quizId,
    Long materialId,
    String materialTitle,
    int correctCount,
    int totalCount,
    Instant attemptedAt
) {

    public static QuizAttemptSummaryResponse from(QuizAttempt attempt) {
        return QuizAttemptSummaryResponse.builder()
            .id(attempt.getId())
            .quizId(attempt.getQuiz().getId())
            .materialId(attempt.getQuiz().getMaterial().getId())
            .materialTitle(attempt.getQuiz().getMaterial().getTitle())
            .correctCount(attempt.getCorrectCount())
            .totalCount(attempt.getTotalCount())
            .attemptedAt(attempt.getAttemptedAt())
            .build();
    }
}
