package com.portfolio.weatheralert.api;

import com.portfolio.weatheralert.service.WeatherIngestService;
import com.portfolio.weatheralert.service.dto.IngestRunResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ingest")
public class IngestController {

    private final WeatherIngestService weatherIngestService;

    public IngestController(WeatherIngestService weatherIngestService) {
        this.weatherIngestService = weatherIngestService;
    }

    @PostMapping("/run")
    public IngestRunResponse runOnce() {
        return weatherIngestService.ingestAllLocations();
    }
}
