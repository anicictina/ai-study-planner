package com.anicictina.backend.ai;

public record RawProposedItem(
    Long subjectId,
    String date,
    String startTime,
    Integer durationMinutes,
    String topic
) {
}
