package com.portfolio.weatheralert.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.portfolio.weatheralert.domain.Subscription;
import com.portfolio.weatheralert.domain.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByLocationIdAndEnabledTrue(UUID locationId);

    List<Subscription> findByUserIdOrderByCreatedAtDesc(UUID userId);

    boolean existsByUserIdAndLocationIdAndRuleTypeAndThreshold(UUID userId, UUID locationId, RuleType ruleType, double threshold);

    Optional<Subscription> findByUserIdAndLocationIdAndRuleTypeAndThreshold(UUID userId, UUID locationId, RuleType ruleType, double threshold);
}
