package com.anicictina.backend.material.dto;

import com.anicictina.backend.material.MaterialStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private MaterialStatus status;
}
