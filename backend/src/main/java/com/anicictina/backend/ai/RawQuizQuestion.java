package com.anicictina.backend.ai;

import java.util.List;

public record RawQuizQuestion(
    String questionText,
    List<String> options,
    Integer correctAnswerIndex,
    String explanation
) {
}
