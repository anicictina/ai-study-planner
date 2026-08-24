package com.anicictina.backend.ai;

public record RejectedQuizQuestion(RawQuizQuestion question, String reason) {
}
