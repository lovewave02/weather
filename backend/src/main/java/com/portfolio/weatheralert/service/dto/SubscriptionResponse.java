package com.portfolio.weatheralert.service.dto;

import java.time.Instant;
import java.util.UUID;

import com.portfolio.weatheralert.domain.RuleType;
import com.portfolio.weatheralert.domain.Subscription;

public record SubscriptionResponse(
        UUID id,
        UUID userId,
        UUID locationId,
        RuleType ruleType,
        double threshold,
        boolean enabled,
        Instant createdAt
) {
    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getUser().getId(),
                subscription.getLocation().getId(),
                subscription.getRuleType(),
                subscription.getThreshold(),
                subscription.isEnabled(),
                subscription.getCreatedAt()
        );
    }
}

