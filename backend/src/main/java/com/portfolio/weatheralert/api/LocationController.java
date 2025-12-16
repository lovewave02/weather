package com.portfolio.weatheralert.api;

import java.util.List;
import java.util.UUID;

import com.portfolio.weatheralert.service.LocationService;
import com.portfolio.weatheralert.service.WeatherForecastService;
import com.portfolio.weatheralert.service.WeatherQueryService;
import com.portfolio.weatheralert.service.dto.CreateLocationRequest;
import com.portfolio.weatheralert.service.dto.CurrentWeatherResponse;
import com.portfolio.weatheralert.service.dto.HourlyWeatherResponse;
import com.portfolio.weatheralert.service.dto.LocationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    private final LocationService locationService;
    private final WeatherQueryService weatherQueryService;
    private final WeatherForecastService weatherForecastService;

    public LocationController(LocationService locationService,
                              WeatherQueryService weatherQueryService,
                              WeatherForecastService weatherForecastService) {
        this.locationService = locationService;
        this.weatherQueryService = weatherQueryService;
        this.weatherForecastService = weatherForecastService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationResponse create(@Valid @RequestBody CreateLocationRequest request) {
        return locationService.create(request);
    }

    @GetMapping
    public List<LocationResponse> list() {
        return locationService.list();
    }

    @GetMapping("/{locationId}/weather/current")
    public CurrentWeatherResponse current(@PathVariable UUID locationId) {
        return weatherQueryService.getCurrent(locationId);
    }

    @GetMapping("/{locationId}/weather/hourly")
    public HourlyWeatherResponse hourly(@PathVariable UUID locationId,
                                        @RequestParam(defaultValue = "24") int hours) {
        int clamped = Math.min(Math.max(hours, 1), 168);
        return weatherForecastService.getHourly(locationId, clamped);
    }
}
