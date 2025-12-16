package com.portfolio.weatheralert.service.dto;

import java.time.Instant;
import java.util.UUID;

import com.portfolio.weatheralert.domain.Location;

public record LocationResponse(
        UUID id,
        String name,
        double latitude,
        double longitude,
        Instant createdAt
) {
    public static LocationResponse from(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getName(),
                location.getLatitude(),
                location.getLongitude(),
                location.getCreatedAt()
        );
    }
}

