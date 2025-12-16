package com.portfolio.weatheralert.service;

import java.util.List;
import java.util.UUID;

import com.portfolio.weatheralert.domain.Location;
import com.portfolio.weatheralert.domain.WeatherSnapshot;
import com.portfolio.weatheralert.repository.LocationRepository;
import com.portfolio.weatheralert.repository.WeatherSnapshotRepository;
import com.portfolio.weatheralert.weather.OpenMeteoClient;
import com.portfolio.weatheralert.weather.WeatherObservation;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeatherIngestService {

    private static final String SOURCE = "open-meteo";

    private final LocationRepository locationRepository;
    private final WeatherSnapshotRepository weatherSnapshotRepository;
    private final OpenMeteoClient openMeteoClient;
    private final AlertEvaluationService alertEvaluationService;
    private final CacheManager cacheManager;

    public WeatherIngestService(LocationRepository locationRepository,
                               WeatherSnapshotRepository weatherSnapshotRepository,
                               OpenMeteoClient openMeteoClient,
                               AlertEvaluationService alertEvaluationService,
                               CacheManager cacheManager) {
        this.locationRepository = locationRepository;
        this.weatherSnapshotRepository = weatherSnapshotRepository;
        this.openMeteoClient = openMeteoClient;
        this.alertEvaluationService = alertEvaluationService;
        this.cacheManager = cacheManager;
    }

    @Scheduled(cron = "0 */5 * * * *")
    @SchedulerLock(name = "weather_ingest", lockAtMostFor = "PT4M", lockAtLeastFor = "PT10S")
    public void scheduledIngest() {
        ingestAllLocations();
    }

    @Transactional
    public void ingestAllLocations() {
        List<Location> locations = locationRepository.findAll();
        for (Location location : locations) {
            openMeteoClient.fetchCurrent(location.getLatitude(), location.getLongitude())
                    .ifPresent(observation -> upsertSnapshot(location, observation));
        }
    }

    private void upsertSnapshot(Location location, WeatherObservation observation) {
        weatherSnapshotRepository.findByLocationIdAndObservedAtAndSource(location.getId(), observation.observedAt(), SOURCE)
                .ifPresentOrElse(existing -> {
                    boolean changed = existing.updateValues(
                            observation.temperatureC(),
                            observation.apparentTemperatureC(),
                            observation.precipitationMm(),
                            observation.weatherCode()
                    );
                    if (changed) {
                        weatherSnapshotRepository.save(existing);
                        evictCurrentWeatherCache(location.getId());
                    }
                }, () -> insertSnapshot(location, observation));
    }

    private void insertSnapshot(Location location, WeatherObservation observation) {
        WeatherSnapshot saved = weatherSnapshotRepository.save(
                new WeatherSnapshot(
                        location,
                        observation.observedAt(),
                        observation.temperatureC(),
                        observation.apparentTemperatureC(),
                        observation.precipitationMm(),
                        observation.weatherCode(),
                        SOURCE
                )
        );
        evictCurrentWeatherCache(location.getId());
        alertEvaluationService.evaluateSnapshot(saved);
    }

    private void evictCurrentWeatherCache(UUID locationId) {
        Cache cache = cacheManager.getCache("currentWeather");
        if (cache != null) {
            cache.evict(locationId);
        }
    }
}
