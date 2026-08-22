package com.anicictina.backend.user.dto;

import com.anicictina.backend.user.PreferredTime;
import com.anicictina.backend.user.User;
import lombok.Builder;

@Builder
public record UserResponse(
    Long id,
    String firstName,
    String lastName,
    String email,
    String role,
    PreferredTime preferredStudyTime
) {

    public static UserResponse from(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .role(user.getRole().name())
            .preferredStudyTime(user.getPreferredStudyTime())
            .build();
    }
}
