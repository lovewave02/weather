package com.portfolio.weatheralert.weather;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weather.provider")
public record WeatherProviderProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
}

