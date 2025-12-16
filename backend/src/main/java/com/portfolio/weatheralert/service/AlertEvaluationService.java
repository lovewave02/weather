package com.portfolio.weatheralert.service;

import java.util.List;

import com.portfolio.weatheralert.domain.AlertEvent;
import com.portfolio.weatheralert.domain.AlertStatus;
import com.portfolio.weatheralert.domain.RuleType;
import com.portfolio.weatheralert.domain.Subscription;
import com.portfolio.weatheralert.domain.WeatherSnapshot;
import com.portfolio.weatheralert.repository.AlertEventRepository;
import com.portfolio.weatheralert.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertEvaluationService {

    private final SubscriptionRepository subscriptionRepository;
    private final AlertEventRepository alertEventRepository;

    public AlertEvaluationService(SubscriptionRepository subscriptionRepository, AlertEventRepository alertEventRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.alertEventRepository = alertEventRepository;
    }

    @Transactional
    public int evaluateSnapshot(WeatherSnapshot snapshot) {
        List<Subscription> subscriptions = subscriptionRepository
                .findByLocationIdAndEnabledTrue(snapshot.getLocation().getId());

        int created = 0;
        for (Subscription subscription : subscriptions) {
            if (!shouldTrigger(subscription, snapshot)) {
                continue;
            }

            String message = buildMessage(subscription, snapshot);
            alertEventRepository.save(new AlertEvent(subscription, snapshot, AlertStatus.PENDING, message));
            created++;
        }

        return created;
    }

    private static boolean shouldTrigger(Subscription subscription, WeatherSnapshot snapshot) {
        RuleType ruleType = subscription.getRuleType();
        double threshold = subscription.getThreshold();

        return switch (ruleType) {
            case TEMP_BELOW -> snapshot.getTemperatureC() != null && snapshot.getTemperatureC() < threshold;
            case TEMP_ABOVE -> snapshot.getTemperatureC() != null && snapshot.getTemperatureC() > threshold;
            case PRECIP_ABOVE -> snapshot.getPrecipitationMm() != null && snapshot.getPrecipitationMm() > threshold;
        };
    }

    private static String buildMessage(Subscription subscription, WeatherSnapshot snapshot) {
        String locationName = snapshot.getLocation().getName();
        String rule = subscription.getRuleType() + " " + subscription.getThreshold();
        return "[" + locationName + "] rule=" + rule
                + " observedAt=" + snapshot.getObservedAt()
                + " tempC=" + snapshot.getTemperatureC()
                + " precipMm=" + snapshot.getPrecipitationMm();
    }
}

