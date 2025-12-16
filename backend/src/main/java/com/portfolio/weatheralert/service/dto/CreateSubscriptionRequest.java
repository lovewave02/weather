package com.portfolio.weatheralert.service.dto;

import java.util.UUID;

import com.portfolio.weatheralert.domain.RuleType;
import jakarta.validation.constraints.NotNull;

public record CreateSubscriptionRequest(
        @NotNull UUID userId,
        @NotNull UUID locationId,
        @NotNull RuleType ruleType,
        @NotNull Double threshold
) {
}

