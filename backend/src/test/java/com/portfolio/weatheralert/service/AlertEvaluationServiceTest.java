package com.portfolio.weatheralert.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.portfolio.weatheralert.domain.AlertEvent;
import com.portfolio.weatheralert.domain.AlertStatus;
import com.portfolio.weatheralert.domain.AppUser;
import com.portfolio.weatheralert.domain.Location;
import com.portfolio.weatheralert.domain.RuleType;
import com.portfolio.weatheralert.domain.Subscription;
import com.portfolio.weatheralert.domain.WeatherSnapshot;
import com.portfolio.weatheralert.repository.AlertEventRepository;
import com.portfolio.weatheralert.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class AlertEvaluationServiceTest {

    private final SubscriptionRepository subscriptionRepository = Mockito.mock(SubscriptionRepository.class);
    private final AlertEventRepository alertEventRepository = Mockito.mock(AlertEventRepository.class);

    private final AlertEvaluationService service = new AlertEvaluationService(subscriptionRepository, alertEventRepository);

    @Test
    void evaluateSnapshot_tempBelow_triggers() {
        Location location = new Location("Seoul", 37.5, 126.9);
        WeatherSnapshot snapshot = new WeatherSnapshot(
                location,
                Instant.parse("2025-01-01T00:00:00Z"),
                5.0,
                5.0,
                0.0,
                null,
                "test"
        );

        Subscription subscription = new Subscription(new AppUser("a@b.com"), location, RuleType.TEMP_BELOW, 10.0);
        given(subscriptionRepository.findByLocationIdAndEnabledTrue(any(UUID.class))).willReturn(List.of(subscription));
        given(alertEventRepository.save(any(AlertEvent.class))).willAnswer(inv -> inv.getArgument(0));

        int created = service.evaluateSnapshot(snapshot);

        assertThat(created).isEqualTo(1);

        ArgumentCaptor<AlertEvent> captor = ArgumentCaptor.forClass(AlertEvent.class);
        verify(alertEventRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AlertStatus.PENDING);
        assertThat(captor.getValue().getMessage()).contains("Seoul");
    }

    @Test
    void evaluateSnapshot_tempAbove_triggers() {
        Location location = new Location("Busan", 35.1, 129.0);
        WeatherSnapshot snapshot = new WeatherSnapshot(
                location,
                Instant.parse("2025-01-01T00:00:00Z"),
                25.0,
                25.0,
                0.0,
                null,
                "test"
        );

        Subscription subscription = new Subscription(new AppUser("a@b.com"), location, RuleType.TEMP_ABOVE, 20.0);
        given(subscriptionRepository.findByLocationIdAndEnabledTrue(any(UUID.class))).willReturn(List.of(subscription));
        given(alertEventRepository.save(any(AlertEvent.class))).willAnswer(inv -> inv.getArgument(0));

        int created = service.evaluateSnapshot(snapshot);

        assertThat(created).isEqualTo(1);
    }

    @Test
    void evaluateSnapshot_precipAbove_triggers() {
        Location location = new Location("Jeju", 33.5, 126.5);
        WeatherSnapshot snapshot = new WeatherSnapshot(
                location,
                Instant.parse("2025-01-01T00:00:00Z"),
                10.0,
                10.0,
                2.0,
                null,
                "test"
        );

        Subscription subscription = new Subscription(new AppUser("a@b.com"), location, RuleType.PRECIP_ABOVE, 1.0);
        given(subscriptionRepository.findByLocationIdAndEnabledTrue(any(UUID.class))).willReturn(List.of(subscription));
        given(alertEventRepository.save(any(AlertEvent.class))).willAnswer(inv -> inv.getArgument(0));

        int created = service.evaluateSnapshot(snapshot);

        assertThat(created).isEqualTo(1);
    }

    @Test
    void evaluateSnapshot_notTriggered_createsNothing() {
        Location location = new Location("Seoul", 37.5, 126.9);
        WeatherSnapshot snapshot = new WeatherSnapshot(
                location,
                Instant.parse("2025-01-01T00:00:00Z"),
                15.0,
                15.0,
                0.0,
                null,
                "test"
        );

        Subscription subscription = new Subscription(new AppUser("a@b.com"), location, RuleType.TEMP_BELOW, 10.0);
        given(subscriptionRepository.findByLocationIdAndEnabledTrue(any(UUID.class))).willReturn(List.of(subscription));
        given(alertEventRepository.save(any(AlertEvent.class))).willAnswer(inv -> inv.getArgument(0));

        int created = service.evaluateSnapshot(snapshot);

        assertThat(created).isEqualTo(0);
        verify(alertEventRepository, Mockito.never()).save(any(AlertEvent.class));
    }
}
