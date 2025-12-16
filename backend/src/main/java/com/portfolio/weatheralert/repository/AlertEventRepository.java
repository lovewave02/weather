package com.portfolio.weatheralert.repository;

import java.util.List;
import java.util.UUID;

import com.portfolio.weatheralert.domain.AlertEvent;
import com.portfolio.weatheralert.domain.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertEventRepository extends JpaRepository<AlertEvent, UUID> {
    List<AlertEvent> findTop50ByStatusOrderByCreatedAtAsc(AlertStatus status);

    List<AlertEvent> findBySubscriptionUserIdOrderByCreatedAtDesc(UUID userId);
}

