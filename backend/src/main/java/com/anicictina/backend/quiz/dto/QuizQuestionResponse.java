package com.anicictina.backend.quiz.dto;

import com.anicictina.backend.quiz.QuizQuestion;
import java.util.List;
import lombok.Builder;

@Builder
public record QuizQuestionResponse(
    Long id,
    String questionText,
    List<String> options
) {

    public static QuizQuestionResponse from(QuizQuestion question) {
        return QuizQuestionResponse.builder()
            .id(question.getId())
            .questionText(question.getQuestionText())
            .options(question.getOptions())
            .build();
    }
}
