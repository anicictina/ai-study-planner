package com.anicictina.backend.reminder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReminderRequest {

    private Long subjectId;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Remind at is required")
    private LocalDateTime remindAt;
}
