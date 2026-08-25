package com.anicictina.backend.statistics.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record DayMinutes(LocalDate date, int minutes) {
}
