package com.portfolio.weatheralert.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.portfolio.weatheralert.domain.WeatherSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherSnapshotRepository extends JpaRepository<WeatherSnapshot, UUID> {
    Optional<WeatherSnapshot> findTopByLocationIdOrderByObservedAtDesc(UUID locationId);

    Optional<WeatherSnapshot> findByLocationIdAndObservedAtAndSource(UUID locationId, Instant observedAt, String source);

    boolean existsByLocationIdAndObservedAtAndSource(UUID locationId, Instant observedAt, String source);
}
