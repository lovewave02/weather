package com.portfolio.weatheralert.service;

import java.util.UUID;

import com.portfolio.weatheralert.domain.AppUser;
import com.portfolio.weatheralert.domain.Location;
import com.portfolio.weatheralert.domain.Subscription;
import com.portfolio.weatheralert.repository.AppUserRepository;
import com.portfolio.weatheralert.repository.LocationRepository;
import com.portfolio.weatheralert.repository.SubscriptionRepository;
import com.portfolio.weatheralert.service.dto.CreateSubscriptionRequest;
import com.portfolio.weatheralert.service.dto.SubscriptionResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionService {

    private final AppUserRepository appUserRepository;
    private final LocationRepository locationRepository;
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(AppUserRepository appUserRepository,
                               LocationRepository locationRepository,
                               SubscriptionRepository subscriptionRepository) {
        this.appUserRepository = appUserRepository;
        this.locationRepository = locationRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public SubscriptionResponse create(CreateSubscriptionRequest request) {
        UUID userId = request.userId();
        UUID locationId = request.locationId();

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("user not found: " + userId));
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new EntityNotFoundException("location not found: " + locationId));

        Subscription saved = subscriptionRepository.save(
                new Subscription(user, location, request.ruleType(), request.threshold())
        );
        return SubscriptionResponse.from(saved);
    }
}

