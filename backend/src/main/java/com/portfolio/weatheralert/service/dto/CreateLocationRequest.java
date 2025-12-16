package com.portfolio.weatheralert.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateLocationRequest(
        @NotBlank String name,
        @NotNull Double latitude,
        @NotNull Double longitude
) {
}

