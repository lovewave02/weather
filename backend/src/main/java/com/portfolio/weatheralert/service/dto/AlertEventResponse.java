package com.portfolio.weatheralert.service.dto;

import java.time.Instant;
import java.util.UUID;

import com.portfolio.weatheralert.domain.AlertEvent;
import com.portfolio.weatheralert.domain.AlertStatus;

public record AlertEventResponse(
        UUID id,
        UUID subscriptionId,
        UUID snapshotId,
        AlertStatus status,
        String message,
        Instant createdAt,
        Instant sentAt
) {
    public static AlertEventResponse from(AlertEvent event) {
        return new AlertEventResponse(
                event.getId(),
                event.getSubscription().getId(),
                event.getSnapshot().getId(),
                event.getStatus(),
                event.getMessage(),
                event.getCreatedAt(),
                event.getSentAt()
        );
    }
}

