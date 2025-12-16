package com.portfolio.weatheralert.weather;

import java.time.Instant;

public record HourlyForecastPoint(
        Instant time,
        Double temperatureC,
        Double apparentTemperatureC,
        Integer weatherCode
) {
}

