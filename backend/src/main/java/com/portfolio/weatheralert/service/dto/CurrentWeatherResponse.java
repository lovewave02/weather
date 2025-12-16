package com.portfolio.weatheralert.service.dto;

import java.time.Instant;
import java.util.UUID;

import com.portfolio.weatheralert.domain.WeatherSnapshot;

public record CurrentWeatherResponse(
        UUID locationId,
        Instant observedAt,
        Double temperatureC,
        Double apparentTemperatureC,
        Double precipitationMm,
        Integer weatherCode,
        String source
) {
    public static CurrentWeatherResponse from(WeatherSnapshot snapshot) {
        return new CurrentWeatherResponse(
                snapshot.getLocation().getId(),
                snapshot.getObservedAt(),
                snapshot.getTemperatureC(),
                snapshot.getApparentTemperatureC(),
                snapshot.getPrecipitationMm(),
                snapshot.getWeatherCode(),
                snapshot.getSource()
        );
    }
}
