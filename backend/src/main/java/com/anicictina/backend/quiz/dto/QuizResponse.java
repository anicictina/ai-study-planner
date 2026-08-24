package com.anicictina.backend.quiz.dto;

import com.anicictina.backend.quiz.Quiz;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record QuizResponse(
    Long id,
    Long materialId,
    String materialTitle,
    Instant createdAt,
    List<QuizQuestionResponse> questions
) {

    public static QuizResponse from(Quiz quiz) {
        return QuizResponse.builder()
            .id(quiz.getId())
            .materialId(quiz.getMaterial().getId())
            .materialTitle(quiz.getMaterial().getTitle())
            .createdAt(quiz.getCreatedAt())
            .questions(quiz.getQuestions().stream().map(QuizQuestionResponse::from).toList())
            .build();
    }
}
