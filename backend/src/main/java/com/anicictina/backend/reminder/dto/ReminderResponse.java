package com.anicictina.backend.reminder.dto;

import com.anicictina.backend.reminder.Reminder;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ReminderResponse(
    Long id,
    Long subjectId,
    String subjectName,
    String subjectColor,
    String message,
    LocalDateTime remindAt,
    boolean dismissed,
    boolean due,
    Instant createdAt
) {

    public static ReminderResponse from(Reminder reminder) {
        boolean isDue = !reminder.isDismissed() && !reminder.getRemindAt().isAfter(LocalDateTime.now());

        return ReminderResponse.builder()
            .id(reminder.getId())
            .subjectId(reminder.getSubject() != null ? reminder.getSubject().getId() : null)
            .subjectName(reminder.getSubject() != null ? reminder.getSubject().getName() : null)
            .subjectColor(reminder.getSubject() != null ? reminder.getSubject().getColor() : null)
            .message(reminder.getMessage())
            .remindAt(reminder.getRemindAt())
            .dismissed(reminder.isDismissed())
            .due(isDue)
            .createdAt(reminder.getCreatedAt())
            .build();
    }
}
