package com.portfolio.weatheralert.service;

import java.util.List;
import java.util.UUID;

import com.portfolio.weatheralert.domain.Location;
import com.portfolio.weatheralert.repository.LocationRepository;
import com.portfolio.weatheralert.service.dto.HourlyWeatherResponse;
import com.portfolio.weatheralert.service.dto.HourlyWeatherResponse.HourlyWeatherPoint;
import com.portfolio.weatheralert.weather.HourlyForecast;
import com.portfolio.weatheralert.weather.HourlyForecastPoint;
import com.portfolio.weatheralert.weather.OpenMeteoClient;
import com.portfolio.weatheralert.weather.WeatherProviderUnavailableException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeatherForecastService {

    private final LocationRepository locationRepository;
    private final OpenMeteoClient openMeteoClient;

    public WeatherForecastService(LocationRepository locationRepository, OpenMeteoClient openMeteoClient) {
        this.locationRepository = locationRepository;
        this.openMeteoClient = openMeteoClient;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "hourlyWeather", key = "#locationId + ':' + #hours")
    public HourlyWeatherResponse getHourly(UUID locationId, int hours) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new EntityNotFoundException("location not found: " + locationId));

        HourlyForecast forecast = openMeteoClient.fetchHourlyForecast(location.getLatitude(), location.getLongitude(), hours)
                .orElseThrow(() -> new WeatherProviderUnavailableException("hourly forecast unavailable"));

        List<HourlyWeatherPoint> points = forecast.points().stream()
                .map(WeatherForecastService::toDto)
                .toList();

        return HourlyWeatherResponse.of(locationId, hours, points);
    }

    private static HourlyWeatherPoint toDto(HourlyForecastPoint point) {
        return new HourlyWeatherPoint(
                point.time(),
                point.temperatureC(),
                point.apparentTemperatureC(),
                point.weatherCode()
        );
    }
}

