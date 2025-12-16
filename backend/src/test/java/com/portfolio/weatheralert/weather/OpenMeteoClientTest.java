package com.portfolio.weatheralert.weather;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class OpenMeteoClientTest {

    @Test
    void fetchCurrent_parsesFields() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .addHeader("Content-Type", "application/json")
                    .setBody("""
                            {
                              "current": {
                                "time": "2025-01-01T00:00",
                                "temperature_2m": 3.5,
                                "apparent_temperature": 1.2,
                                "precipitation": 0.2,
                                "weather_code": 3
                              }
                            }
                            """));
            server.start();

            RestClient restClient = RestClient.builder()
                    .baseUrl(server.url("/").toString())
                    .build();
            OpenMeteoClient client = new OpenMeteoClient(restClient);

            WeatherObservation observation = client.fetchCurrent(37.5, 126.9).orElseThrow();
            assertThat(observation.temperatureC()).isEqualTo(3.5);
            assertThat(observation.apparentTemperatureC()).isEqualTo(1.2);
            assertThat(observation.precipitationMm()).isEqualTo(0.2);
            assertThat(observation.weatherCode()).isEqualTo(3);
            assertThat(observation.observedAt()).isEqualTo(Instant.parse("2025-01-01T00:00:00Z"));
        }
    }
}
