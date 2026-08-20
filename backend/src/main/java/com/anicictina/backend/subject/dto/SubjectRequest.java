package com.anicictina.backend.subject.dto;

import com.anicictina.backend.subject.Level;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Credits (ESPB) are required")
    @Min(value = 1, message = "Credits must be at least 1")
    private Integer credits;

    @NotNull(message = "Difficulty is required")
    private Level difficulty;

    @NotNull(message = "Priority is required")
    private Level priority;

    @Min(value = 0, message = "Knowledge percent must be between 0 and 100")
    @Max(value = 100, message = "Knowledge percent must be between 0 and 100")
    private Integer knowledgePercent;

    private String color;
}
