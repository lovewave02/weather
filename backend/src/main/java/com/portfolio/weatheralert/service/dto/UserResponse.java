package com.portfolio.weatheralert.service.dto;

import java.time.Instant;
import java.util.UUID;

import com.portfolio.weatheralert.domain.AppUser;

public record UserResponse(
        UUID id,
        String email,
        Instant createdAt
) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getCreatedAt());
    }
}

