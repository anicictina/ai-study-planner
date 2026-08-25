package com.anicictina.backend.statistics.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record AttemptScore(Instant attemptedAt, String materialTitle, double scorePercent) {
}
