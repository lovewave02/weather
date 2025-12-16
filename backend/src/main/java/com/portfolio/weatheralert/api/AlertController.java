package com.portfolio.weatheralert.api;

import java.util.List;
import java.util.UUID;

import com.portfolio.weatheralert.service.AlertQueryService;
import com.portfolio.weatheralert.service.dto.AlertEventResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{userId}/alerts")
public class AlertController {

    private final AlertQueryService alertQueryService;

    public AlertController(AlertQueryService alertQueryService) {
        this.alertQueryService = alertQueryService;
    }

    @GetMapping
    public List<AlertEventResponse> list(@PathVariable UUID userId) {
        return alertQueryService.listForUser(userId);
    }
}

