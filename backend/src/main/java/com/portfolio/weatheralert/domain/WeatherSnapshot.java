package com.portfolio.weatheralert.domain;

import java.time.Instant;
import java.util.UUID;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(
        name = "weather_snapshots",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_weather_location_observed_source",
                        columnNames = {"location_id", "observed_at", "source"}
                )
        }
)
public class WeatherSnapshot extends AuditedEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    @Column(name = "temperature_c")
    private Double temperatureC;

    @Column(name = "apparent_temperature_c")
    private Double apparentTemperatureC;

    @Column(name = "precipitation_mm")
    private Double precipitationMm;

    @Column(name = "weather_code")
    private Integer weatherCode;

    @Column(nullable = false, length = 64)
    private String source;

    protected WeatherSnapshot() {
    }

    public WeatherSnapshot(Location location,
                           Instant observedAt,
                           Double temperatureC,
                           Double apparentTemperatureC,
                           Double precipitationMm,
                           Integer weatherCode,
                           String source) {
        this.location = location;
        this.observedAt = observedAt;
        this.temperatureC = temperatureC;
        this.apparentTemperatureC = apparentTemperatureC;
        this.precipitationMm = precipitationMm;
        this.weatherCode = weatherCode;
        this.source = source;
    }

    public UUID getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public Instant getObservedAt() {
        return observedAt;
    }

    public Double getTemperatureC() {
        return temperatureC;
    }

    public Double getApparentTemperatureC() {
        return apparentTemperatureC;
    }

    public Double getPrecipitationMm() {
        return precipitationMm;
    }

    public Integer getWeatherCode() {
        return weatherCode;
    }

    public boolean updateValues(Double temperatureC, Double apparentTemperatureC, Double precipitationMm, Integer weatherCode) {
        boolean changed = false;

        if (temperatureC != null && !Objects.equals(this.temperatureC, temperatureC)) {
            this.temperatureC = temperatureC;
            changed = true;
        }
        if (apparentTemperatureC != null && !Objects.equals(this.apparentTemperatureC, apparentTemperatureC)) {
            this.apparentTemperatureC = apparentTemperatureC;
            changed = true;
        }
        if (precipitationMm != null && !Objects.equals(this.precipitationMm, precipitationMm)) {
            this.precipitationMm = precipitationMm;
            changed = true;
        }
        if (weatherCode != null && !Objects.equals(this.weatherCode, weatherCode)) {
            this.weatherCode = weatherCode;
            changed = true;
        }

        return changed;
    }

    public String getSource() {
        return source;
    }
}
