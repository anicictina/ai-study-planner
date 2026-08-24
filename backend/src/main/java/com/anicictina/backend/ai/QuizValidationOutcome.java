package com.anicictina.backend.ai;

import java.util.List;

public record QuizValidationOutcome(
    List<RawQuizQuestion> validQuestions,
    List<RejectedQuizQuestion> rejectedQuestions
) {
}
