package com.portfolio.weatheralert.service;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cache")
public record CacheTtlProperties(
        Duration currentWeatherTtl,
        Duration hourlyWeatherTtl
) {
}
