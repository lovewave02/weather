package com.portfolio.weatheralert.service;

import java.util.UUID;

import com.portfolio.weatheralert.domain.Location;
import com.portfolio.weatheralert.domain.WeatherSnapshot;
import com.portfolio.weatheralert.repository.LocationRepository;
import com.portfolio.weatheralert.repository.WeatherSnapshotRepository;
import com.portfolio.weatheralert.service.dto.CurrentWeatherResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeatherQueryService {

    private final LocationRepository locationRepository;
    private final WeatherSnapshotRepository weatherSnapshotRepository;

    public WeatherQueryService(LocationRepository locationRepository, WeatherSnapshotRepository weatherSnapshotRepository) {
        this.locationRepository = locationRepository;
        this.weatherSnapshotRepository = weatherSnapshotRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "currentWeather", key = "#locationId")
    public CurrentWeatherResponse getCurrent(UUID locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new EntityNotFoundException("location not found: " + locationId));
        WeatherSnapshot snapshot = weatherSnapshotRepository.findTopByLocationIdOrderByObservedAtDesc(location.getId())
                .orElseThrow(() -> new EntityNotFoundException("no snapshot for location: " + locationId));

        return CurrentWeatherResponse.from(snapshot);
    }
}
