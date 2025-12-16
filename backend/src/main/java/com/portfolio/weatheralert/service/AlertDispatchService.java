package com.portfolio.weatheralert.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.portfolio.weatheralert.domain.AlertEvent;
import com.portfolio.weatheralert.domain.AlertStatus;
import com.portfolio.weatheralert.repository.AlertEventRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertDispatchService {

    private static final Logger log = LoggerFactory.getLogger(AlertDispatchService.class);

    private final AlertEventRepository alertEventRepository;
    private final Clock clock;

    public AlertDispatchService(AlertEventRepository alertEventRepository, Clock clock) {
        this.alertEventRepository = alertEventRepository;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "PT30S")
    @SchedulerLock(name = "alert_dispatch", lockAtMostFor = "PT2M", lockAtLeastFor = "PT5S")
    @Transactional
    public void dispatchPending() {
        List<AlertEvent> pending = alertEventRepository.findTop50ByStatusOrderByCreatedAtAsc(AlertStatus.PENDING);
        Instant sentAt = clock.instant();

        for (AlertEvent event : pending) {
            log.info("ALERT_SEND id={} message={}", event.getId(), event.getMessage());
            event.markSent(sentAt);
        }
        if (!pending.isEmpty()) {
            log.info("ALERT_DISPATCHED count={}", pending.size());
        }
    }
}
