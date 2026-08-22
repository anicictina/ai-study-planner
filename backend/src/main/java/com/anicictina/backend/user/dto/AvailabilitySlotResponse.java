package com.anicictina.backend.user.dto;

import com.anicictina.backend.user.AvailabilitySlot;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record AvailabilitySlotResponse(
    Long id,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime
) {

    public static AvailabilitySlotResponse from(AvailabilitySlot slot) {
        return AvailabilitySlotResponse.builder()
            .id(slot.getId())
            .dayOfWeek(slot.getDayOfWeek())
            .startTime(slot.getStartTime())
            .endTime(slot.getEndTime())
            .build();
    }
}
