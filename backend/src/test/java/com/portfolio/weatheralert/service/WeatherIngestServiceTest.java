package com.portfolio.weatheralert.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import com.portfolio.weatheralert.domain.Location;
import com.portfolio.weatheralert.domain.WeatherSnapshot;
import com.portfolio.weatheralert.repository.LocationRepository;
import com.portfolio.weatheralert.repository.WeatherSnapshotRepository;
import com.portfolio.weatheralert.service.dto.IngestRunResponse;
import com.portfolio.weatheralert.weather.OpenMeteoClient;
import com.portfolio.weatheralert.weather.WeatherObservation;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

class WeatherIngestServiceTest {

    private final LocationRepository locationRepository = Mockito.mock(LocationRepository.class);
    private final WeatherSnapshotRepository weatherSnapshotRepository = Mockito.mock(WeatherSnapshotRepository.class);
    private final OpenMeteoClient openMeteoClient = Mockito.mock(OpenMeteoClient.class);
    private final AlertEvaluationService alertEvaluationService = Mockito.mock(AlertEvaluationService.class);
    private final CacheManager cacheManager = Mockito.mock(CacheManager.class);
    private final Cache currentWeatherCache = Mockito.mock(Cache.class);

    private final WeatherIngestService service = new WeatherIngestService(
            locationRepository,
            weatherSnapshotRepository,
            openMeteoClient,
            alertEvaluationService,
            cacheManager
    );

    @Test
    void ingestAllLocations_reportsInsertedUpdatedUnchangedAndMisses() {
        Location insertedLocation = new Location("Seoul", 37.5665, 126.9780);
        Location updatedLocation = new Location("Busan", 35.1796, 129.0756);
        Location unchangedLocation = new Location("Jeju", 33.4996, 126.5312);
        Location missingLocation = new Location("Missing", 0, 0);

        Instant insertedAt = Instant.parse("2026-06-28T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-06-28T01:00:00Z");
        Instant unchangedAt = Instant.parse("2026-06-28T02:00:00Z");

        WeatherObservation insertedObservation = new WeatherObservation(insertedAt, 23.0, 24.1, 0.0, 1);
        WeatherObservation updatedObservation = new WeatherObservation(updatedAt, 24.0, 25.2, 1.1, 3);
        WeatherObservation unchangedObservation = new WeatherObservation(unchangedAt, 25.0, 26.0, 0.0, 0);

        WeatherSnapshot existingUpdated = new WeatherSnapshot(updatedLocation, updatedAt, 21.0, 22.0, 0.0, 1, "open-meteo");
        WeatherSnapshot existingUnchanged = new WeatherSnapshot(unchangedLocation, unchangedAt, 25.0, 26.0, 0.0, 0, "open-meteo");

        given(locationRepository.findAll()).willReturn(List.of(insertedLocation, updatedLocation, unchangedLocation, missingLocation));
        given(openMeteoClient.fetchCurrent(insertedLocation.getLatitude(), insertedLocation.getLongitude())).willReturn(Optional.of(insertedObservation));
        given(openMeteoClient.fetchCurrent(updatedLocation.getLatitude(), updatedLocation.getLongitude())).willReturn(Optional.of(updatedObservation));
        given(openMeteoClient.fetchCurrent(unchangedLocation.getLatitude(), unchangedLocation.getLongitude())).willReturn(Optional.of(unchangedObservation));
        given(openMeteoClient.fetchCurrent(missingLocation.getLatitude(), missingLocation.getLongitude())).willReturn(Optional.empty());

        given(weatherSnapshotRepository.findByLocationIdAndObservedAtAndSource(insertedLocation.getId(), insertedAt, "open-meteo"))
                .willReturn(Optional.empty());
        given(weatherSnapshotRepository.findByLocationIdAndObservedAtAndSource(updatedLocation.getId(), updatedAt, "open-meteo"))
                .willReturn(Optional.of(existingUpdated));
        given(weatherSnapshotRepository.findByLocationIdAndObservedAtAndSource(unchangedLocation.getId(), unchangedAt, "open-meteo"))
                .willReturn(Optional.of(existingUnchanged));

        given(weatherSnapshotRepository.save(Mockito.any(WeatherSnapshot.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(alertEvaluationService.evaluateSnapshot(Mockito.any(WeatherSnapshot.class))).willReturn(2);
        given(cacheManager.getCache("currentWeather")).willReturn(currentWeatherCache);

        IngestRunResponse result = service.ingestAllLocations();

        assertThat(result.totalLocations()).isEqualTo(4);
        assertThat(result.fetchedLocations()).isEqualTo(3);
        assertThat(result.insertedSnapshots()).isEqualTo(1);
        assertThat(result.updatedSnapshots()).isEqualTo(1);
        assertThat(result.unchangedSnapshots()).isEqualTo(1);
        assertThat(result.providerMisses()).isEqualTo(1);
        assertThat(result.alertsCreated()).isEqualTo(2);

        verify(alertEvaluationService).evaluateSnapshot(Mockito.any(WeatherSnapshot.class));
        verify(cacheManager, Mockito.times(2)).getCache("currentWeather");
        verify(currentWeatherCache, Mockito.times(2)).evict(Mockito.any());
    }

    @Test
    void ingestAllLocations_skipsCacheEvictionWhenNoCacheExists() {
        Location location = new Location("Seoul", 37.5665, 126.9780);
        Instant observedAt = Instant.parse("2026-06-28T03:00:00Z");
        WeatherObservation observation = new WeatherObservation(observedAt, 22.0, 23.0, 0.5, 2);

        given(locationRepository.findAll()).willReturn(List.of(location));
        given(openMeteoClient.fetchCurrent(location.getLatitude(), location.getLongitude())).willReturn(Optional.of(observation));
        given(weatherSnapshotRepository.findByLocationIdAndObservedAtAndSource(location.getId(), observedAt, "open-meteo"))
                .willReturn(Optional.empty());
        given(weatherSnapshotRepository.save(Mockito.any(WeatherSnapshot.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(cacheManager.getCache("currentWeather")).willReturn(null);

        IngestRunResponse result = service.ingestAllLocations();

        assertThat(result.insertedSnapshots()).isEqualTo(1);
        assertThat(result.providerMisses()).isZero();
        verify(alertEvaluationService).evaluateSnapshot(Mockito.any(WeatherSnapshot.class));
        verify(cacheManager).getCache("currentWeather");
        verify(currentWeatherCache, never()).evict(Mockito.any());
    }
}
