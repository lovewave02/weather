package com.portfolio.weatheralert.repository;

import java.util.List;
import java.util.UUID;

import com.portfolio.weatheralert.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByLocationIdAndEnabledTrue(UUID locationId);
}

