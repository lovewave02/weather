package com.portfolio.weatheralert.service;

import java.util.List;
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

        Subscription existing = subscriptionRepository.findByUserIdAndLocationIdAndRuleTypeAndThreshold(
                userId,
                locationId,
                request.ruleType(),
                request.threshold()
        ).orElse(null);

        if (existing != null) {
            if (existing.isEnabled()) {
                throw new DuplicateResourceException(
                        "subscription already exists for user/location/rule/threshold"
                );
            }
            existing.enable();
            return SubscriptionResponse.from(existing);
        }

        Subscription saved = subscriptionRepository.save(
                new Subscription(user, location, request.ruleType(), request.threshold())
        );
        return SubscriptionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> listForUser(UUID userId) {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    @Transactional
    public SubscriptionResponse disable(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("subscription not found: " + subscriptionId));
        subscription.disable();
        return SubscriptionResponse.from(subscription);
    }

    @Transactional
    public SubscriptionResponse enable(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("subscription not found: " + subscriptionId));
        subscription.enable();
        return SubscriptionResponse.from(subscription);
    }
}
