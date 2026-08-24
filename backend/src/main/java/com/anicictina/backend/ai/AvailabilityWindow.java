package com.anicictina.backend.ai;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilityWindow(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
}
