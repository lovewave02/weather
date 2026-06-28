package com.portfolio.weatheralert.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import com.portfolio.weatheralert.domain.AlertEvent;
import com.portfolio.weatheralert.domain.AlertStatus;
import com.portfolio.weatheralert.domain.AppUser;
import com.portfolio.weatheralert.domain.Location;
import com.portfolio.weatheralert.domain.RuleType;
import com.portfolio.weatheralert.domain.Subscription;
import com.portfolio.weatheralert.domain.WeatherSnapshot;
import com.portfolio.weatheralert.repository.AlertEventRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AlertDispatchServiceTest {

    private final AlertEventRepository alertEventRepository = Mockito.mock(AlertEventRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-28T02:40:00Z"), ZoneOffset.UTC);

    private final AlertDispatchService service = new AlertDispatchService(alertEventRepository, clock);

    @Test
    void dispatchPending_marksAlertsSentAndReturnsCount() {
        Location location = new Location("Seoul", 37.5, 126.9);
        WeatherSnapshot snapshot = new WeatherSnapshot(
                location,
                Instant.parse("2026-06-28T02:30:00Z"),
                27.0,
                28.0,
                0.0,
                null,
                "test"
        );
        Subscription subscription = new Subscription(new AppUser("a@b.com"), location, RuleType.TEMP_ABOVE, -100.0);
        AlertEvent event = new AlertEvent(subscription, snapshot, AlertStatus.PENDING, "message");

        given(alertEventRepository.findTop50ByStatusOrderByCreatedAtAsc(AlertStatus.PENDING)).willReturn(List.of(event));

        int dispatched = service.dispatchPendingNow();

        assertThat(dispatched).isEqualTo(1);
        assertThat(event.getStatus()).isEqualTo(AlertStatus.SENT);
        assertThat(event.getSentAt()).isEqualTo(clock.instant());
    }

    @Test
    void dispatchPending_returnsZeroWhenNoAlertsExist() {
        given(alertEventRepository.findTop50ByStatusOrderByCreatedAtAsc(AlertStatus.PENDING)).willReturn(List.of());

        int dispatched = service.dispatchPendingNow();

        assertThat(dispatched).isZero();
        verify(alertEventRepository).findTop50ByStatusOrderByCreatedAtAsc(AlertStatus.PENDING);
    }
}
