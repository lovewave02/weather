package com.portfolio.weatheralert.weather;

import java.time.Instant;

public record WeatherObservation(
        Instant observedAt,
        Double temperatureC,
        Double apparentTemperatureC,
        Double precipitationMm,
        Integer weatherCode
) {
}
