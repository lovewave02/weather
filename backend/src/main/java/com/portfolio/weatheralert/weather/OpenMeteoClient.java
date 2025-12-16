package com.portfolio.weatheralert.weather;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenMeteoClient {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoClient.class);

    private final RestClient restClient;

    public OpenMeteoClient(RestClient openMeteoRestClient) {
        this.restClient = openMeteoRestClient;
    }

    @Retry(name = "openMeteo")
    @RateLimiter(name = "openMeteo")
    @CircuitBreaker(name = "openMeteo", fallbackMethod = "fetchCurrentFallback")
    public Optional<WeatherObservation> fetchCurrent(double latitude, double longitude) {
        OpenMeteoResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current", "temperature_2m,apparent_temperature,precipitation,weather_code")
                        .queryParam("timezone", "UTC")
                        .build())
                .retrieve()
                .body(OpenMeteoResponse.class);

        if (response == null || response.current == null) {
            return Optional.empty();
        }

        Instant observedAt = parseToInstant(response.current.time);
        return Optional.of(new WeatherObservation(
                observedAt,
                response.current.temperature2m,
                response.current.apparentTemperature,
                response.current.precipitation,
                response.current.weatherCode
        ));
    }

    @SuppressWarnings("unused")
    private Optional<WeatherObservation> fetchCurrentFallback(double latitude, double longitude, Throwable throwable) {
        log.warn("open-meteo failed lat={}, lon={}, err={}", latitude, longitude, throwable.toString());
        return Optional.empty();
    }

    @Retry(name = "openMeteo")
    @RateLimiter(name = "openMeteo")
    @CircuitBreaker(name = "openMeteo", fallbackMethod = "fetchHourlyForecastFallback")
    public Optional<HourlyForecast> fetchHourlyForecast(double latitude, double longitude, int forecastHours) {
        int hours = clampForecastHours(forecastHours);

        OpenMeteoResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("hourly", "temperature_2m,apparent_temperature,weather_code")
                        .queryParam("forecast_hours", hours)
                        .queryParam("timezone", "UTC")
                        .build())
                .retrieve()
                .body(OpenMeteoResponse.class);

        if (response == null || response.hourly == null || response.hourly.time == null || response.hourly.time.isEmpty()) {
            return Optional.empty();
        }

        List<String> times = response.hourly.time;
        int size = times.size();
        size = Math.min(size, safeSize(response.hourly.temperature2m));
        size = Math.min(size, safeSize(response.hourly.apparentTemperature));
        size = Math.min(size, safeSize(response.hourly.weatherCode));

        List<HourlyForecastPoint> points = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Instant time = parseToInstant(times.get(i));
            Double temperatureC = getOrNull(response.hourly.temperature2m, i);
            Double apparentTemperatureC = getOrNull(response.hourly.apparentTemperature, i);
            Integer weatherCode = getOrNull(response.hourly.weatherCode, i);
            points.add(new HourlyForecastPoint(time, temperatureC, apparentTemperatureC, weatherCode));
        }

        return Optional.of(new HourlyForecast(points));
    }

    @SuppressWarnings("unused")
    private Optional<HourlyForecast> fetchHourlyForecastFallback(double latitude, double longitude, int forecastHours, Throwable throwable) {
        log.warn("open-meteo hourly failed lat={}, lon={}, hours={}, err={}", latitude, longitude, forecastHours, throwable.toString());
        return Optional.empty();
    }

    private static Instant parseToInstant(String time) {
        try {
            return OffsetDateTime.parse(time).toInstant();
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDateTime.parse(time).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse open-meteo time='{}', fallback to now()", time);
            return Instant.now();
        }
    }

    private static int clampForecastHours(int forecastHours) {
        if (forecastHours <= 0) return 24;
        return Math.min(forecastHours, 168);
    }

    private static int safeSize(List<?> list) {
        return list == null ? 0 : list.size();
    }

    private static <T> T getOrNull(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class OpenMeteoResponse {
        @JsonProperty("current")
        private Current current;

        @JsonProperty("hourly")
        private Hourly hourly;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class Current {
        @JsonProperty("time")
        private String time;

        @JsonProperty("temperature_2m")
        private Double temperature2m;

        @JsonProperty("apparent_temperature")
        private Double apparentTemperature;

        @JsonProperty("precipitation")
        private Double precipitation;

        @JsonProperty("weather_code")
        private Integer weatherCode;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class Hourly {
        @JsonProperty("time")
        private List<String> time;

        @JsonProperty("temperature_2m")
        private List<Double> temperature2m;

        @JsonProperty("apparent_temperature")
        private List<Double> apparentTemperature;

        @JsonProperty("weather_code")
        private List<Integer> weatherCode;
    }
}
