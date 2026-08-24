package com.anicictina.backend.statistics.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record QuizStatsResponse(double averageScorePercent, List<AttemptScore> recentAttempts) {
}
