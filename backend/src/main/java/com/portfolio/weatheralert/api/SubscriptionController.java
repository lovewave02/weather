package com.portfolio.weatheralert.api;

import java.util.List;
import java.util.UUID;

import com.portfolio.weatheralert.service.SubscriptionService;
import com.portfolio.weatheralert.service.dto.CreateSubscriptionRequest;
import com.portfolio.weatheralert.service.dto.SubscriptionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse create(@Valid @RequestBody CreateSubscriptionRequest request) {
        return subscriptionService.create(request);
    }

    @GetMapping
    public List<SubscriptionResponse> listForUser(@RequestParam UUID userId) {
        return subscriptionService.listForUser(userId);
    }

    @PostMapping("/{subscriptionId}/disable")
    public SubscriptionResponse disable(@PathVariable UUID subscriptionId) {
        return subscriptionService.disable(subscriptionId);
    }

    @PostMapping("/{subscriptionId}/enable")
    public SubscriptionResponse enable(@PathVariable UUID subscriptionId) {
        return subscriptionService.enable(subscriptionId);
    }
}
