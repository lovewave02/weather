package com.portfolio.weatheralert.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(
        name = "alert_events",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_alert_subscription_snapshot", columnNames = {"subscription_id", "snapshot_id"})
        }
)
public class AlertEvent extends AuditedEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "snapshot_id", nullable = false)
    private WeatherSnapshot snapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AlertStatus status;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "sent_at")
    private Instant sentAt;

    protected AlertEvent() {
    }

    public AlertEvent(Subscription subscription, WeatherSnapshot snapshot, AlertStatus status, String message) {
        this.subscription = subscription;
        this.snapshot = snapshot;
        this.status = status;
        this.message = message;
    }

    public UUID getId() {
        return id;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public WeatherSnapshot getSnapshot() {
        return snapshot;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void markSent(Instant sentAt) {
        this.status = AlertStatus.SENT;
        this.sentAt = sentAt;
    }
}
