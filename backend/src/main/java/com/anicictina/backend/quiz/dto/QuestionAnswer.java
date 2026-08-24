package com.anicictina.backend.quiz.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionAnswer {

    @NotNull(message = "Question id is required")
    private Long questionId;

    @NotNull(message = "Selected index is required")
    private Integer selectedIndex;
}
