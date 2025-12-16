package com.portfolio.weatheralert.service.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public record HourlyWeatherResponse(
        UUID locationId,
        int hours,
        Instant fetchedAt,
        List<HourlyWeatherPoint> points,
        TemperatureStats temperature,
        TemperatureStats apparentTemperature
) {
    public record HourlyWeatherPoint(
            Instant time,
            Double temperatureC,
            Double apparentTemperatureC,
            Integer weatherCode
    ) {
    }

    public record TemperatureStats(
            Double min,
            Double max,
            Double avg
    ) {
    }

    public static HourlyWeatherResponse of(UUID locationId, int hours, List<HourlyWeatherPoint> points) {
        TemperatureStats temperature = stats(points, HourlyWeatherPoint::temperatureC);
        TemperatureStats apparentTemperature = stats(points, HourlyWeatherPoint::apparentTemperatureC);
        return new HourlyWeatherResponse(locationId, hours, Instant.now(), points, temperature, apparentTemperature);
    }

    private static TemperatureStats stats(List<HourlyWeatherPoint> points, Function<HourlyWeatherPoint, Double> extractor) {
        if (points == null || points.isEmpty()) {
            return new TemperatureStats(null, null, null);
        }

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double sum = 0;
        int count = 0;

        for (HourlyWeatherPoint point : points) {
            if (point == null) continue;
            Double value = extractor.apply(point);
            if (value == null) continue;

            double v = value;
            min = Math.min(min, v);
            max = Math.max(max, v);
            sum += v;
            count++;
        }

        if (count == 0) {
            return new TemperatureStats(null, null, null);
        }

        return new TemperatureStats(min, max, sum / count);
    }
}

