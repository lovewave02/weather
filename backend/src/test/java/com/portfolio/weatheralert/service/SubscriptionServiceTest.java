package com.portfolio.weatheralert.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.portfolio.weatheralert.domain.RuleType;
import com.portfolio.weatheralert.domain.Subscription;
import com.portfolio.weatheralert.repository.AppUserRepository;
import com.portfolio.weatheralert.repository.LocationRepository;
import com.portfolio.weatheralert.repository.SubscriptionRepository;
import com.portfolio.weatheralert.service.dto.SubscriptionResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SubscriptionServiceTest {

    private final AppUserRepository appUserRepository = Mockito.mock(AppUserRepository.class);
    private final LocationRepository locationRepository = Mockito.mock(LocationRepository.class);
    private final SubscriptionRepository subscriptionRepository = Mockito.mock(SubscriptionRepository.class);

    private final SubscriptionService service = new SubscriptionService(
            appUserRepository,
            locationRepository,
            subscriptionRepository
    );

    @Test
    void listForUser_returnsMappedSubscriptions() {
        UUID userId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();

        Subscription subscription = Mockito.mock(Subscription.class);
        given(subscription.getId()).willReturn(subscriptionId);
        given(subscription.getRuleType()).willReturn(RuleType.TEMP_ABOVE);
        given(subscription.getThreshold()).willReturn(28.0);
        given(subscription.isEnabled()).willReturn(true);
        given(subscription.getCreatedAt()).willReturn(Instant.parse("2026-06-28T00:00:00Z"));

        var user = Mockito.mock(com.portfolio.weatheralert.domain.AppUser.class);
        given(user.getId()).willReturn(userId);
        given(subscription.getUser()).willReturn(user);

        var location = Mockito.mock(com.portfolio.weatheralert.domain.Location.class);
        given(location.getId()).willReturn(locationId);
        given(subscription.getLocation()).willReturn(location);

        given(subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId)).willReturn(List.of(subscription));

        List<SubscriptionResponse> responses = service.listForUser(userId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).userId()).isEqualTo(userId);
        assertThat(responses.get(0).locationId()).isEqualTo(locationId);
        assertThat(responses.get(0).ruleType()).isEqualTo(RuleType.TEMP_ABOVE);
    }
}
