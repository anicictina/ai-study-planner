package com.anicictina.backend.auth.dto;

import com.anicictina.backend.user.User;
import lombok.Builder;

@Builder
public record AuthResponse(
    String token,
    Long id,
    String firstName,
    String lastName,
    String email,
    String role
) {

    public static AuthResponse from(User user, String token) {
        return AuthResponse.builder()
            .token(token)
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .role(user.getRole().name())
            .build();
    }
}
