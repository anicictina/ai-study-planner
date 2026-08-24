package com.anicictina.backend.quiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizSubmitRequest {

    @NotEmpty(message = "Answers are required")
    @Valid
    private List<QuestionAnswer> answers;
}
