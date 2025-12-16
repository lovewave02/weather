package com.portfolio.weatheralert.api;

import com.portfolio.weatheralert.service.WeatherIngestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ingest")
public class IngestController {

    private final WeatherIngestService weatherIngestService;

    public IngestController(WeatherIngestService weatherIngestService) {
        this.weatherIngestService = weatherIngestService;
    }

    @PostMapping("/run")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void runOnce() {
        weatherIngestService.ingestAllLocations();
    }
}

