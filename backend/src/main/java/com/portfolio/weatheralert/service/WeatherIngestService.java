package com.portfolio.weatheralert.service;

import java.util.List;
import java.util.UUID;

import com.portfolio.weatheralert.domain.Location;
import com.portfolio.weatheralert.domain.WeatherSnapshot;
import com.portfolio.weatheralert.repository.LocationRepository;
import com.portfolio.weatheralert.repository.WeatherSnapshotRepository;
import com.portfolio.weatheralert.service.dto.IngestRunResponse;
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
    public IngestRunResponse ingestAllLocations() {
        List<Location> locations = locationRepository.findAll();
        int fetchedLocations = 0;
        int insertedSnapshots = 0;
        int updatedSnapshots = 0;
        int unchangedSnapshots = 0;
        int providerMisses = 0;
        int alertsCreated = 0;

        for (Location location : locations) {
            WeatherObservation observation = openMeteoClient.fetchCurrent(location.getLatitude(), location.getLongitude()).orElse(null);
            if (observation == null) {
                providerMisses++;
                continue;
            }

            fetchedLocations++;
            IngestOutcome outcome = upsertSnapshot(location, observation);
            switch (outcome.status()) {
                case INSERTED -> {
                    insertedSnapshots++;
                    alertsCreated += outcome.alertsCreated();
                }
                case UPDATED -> updatedSnapshots++;
                case UNCHANGED -> unchangedSnapshots++;
            }
        }

        return new IngestRunResponse(
                locations.size(),
                fetchedLocations,
                insertedSnapshots,
                updatedSnapshots,
                unchangedSnapshots,
                providerMisses,
                alertsCreated
        );
    }

    private IngestOutcome upsertSnapshot(Location location, WeatherObservation observation) {
        return weatherSnapshotRepository.findByLocationIdAndObservedAtAndSource(location.getId(), observation.observedAt(), SOURCE)
                .map(existing -> {
                    boolean changed = existing.updateValues(
                            observation.temperatureC(),
                            observation.apparentTemperatureC(),
                            observation.precipitationMm(),
                            observation.weatherCode()
                    );
                    if (changed) {
                        weatherSnapshotRepository.save(existing);
                        evictCurrentWeatherCache(location.getId());
                        return IngestOutcome.updated();
                    }
                    return IngestOutcome.unchanged();
                })
                .orElseGet(() -> insertSnapshot(location, observation));
    }

    private IngestOutcome insertSnapshot(Location location, WeatherObservation observation) {
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
        int alertsCreated = alertEvaluationService.evaluateSnapshot(saved);
        return IngestOutcome.inserted(alertsCreated);
    }

    private void evictCurrentWeatherCache(UUID locationId) {
        Cache cache = cacheManager.getCache("currentWeather");
        if (cache != null) {
            cache.evict(locationId);
        }
    }

    private record IngestOutcome(IngestStatus status, int alertsCreated) {
        static IngestOutcome inserted(int alertsCreated) {
            return new IngestOutcome(IngestStatus.INSERTED, alertsCreated);
        }

        static IngestOutcome updated() {
            return new IngestOutcome(IngestStatus.UPDATED, 0);
        }

        static IngestOutcome unchanged() {
            return new IngestOutcome(IngestStatus.UNCHANGED, 0);
        }
    }

    private enum IngestStatus {
        INSERTED,
        UPDATED,
        UNCHANGED
    }
}
