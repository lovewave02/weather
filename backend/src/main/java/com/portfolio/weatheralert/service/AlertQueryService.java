package com.portfolio.weatheralert.service;

import java.util.List;
import java.util.UUID;

import com.portfolio.weatheralert.repository.AlertEventRepository;
import com.portfolio.weatheralert.service.dto.AlertEventResponse;
import org.springframework.stereotype.Service;

@Service
public class AlertQueryService {

    private final AlertEventRepository alertEventRepository;

    public AlertQueryService(AlertEventRepository alertEventRepository) {
        this.alertEventRepository = alertEventRepository;
    }

    public List<AlertEventResponse> listForUser(UUID userId) {
        return alertEventRepository.findBySubscriptionUserIdOrderByCreatedAtDesc(userId).stream()
                .map(AlertEventResponse::from)
                .toList();
    }
}

