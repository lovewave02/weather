package com.portfolio.weatheralert.domain;

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
        name = "subscriptions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_subscriptions_user_location_rule_threshold",
                        columnNames = {"user_id", "location_id", "rule_type", "threshold"}
                )
        }
)
public class Subscription extends AuditedEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 32)
    private RuleType ruleType;

    @Column(nullable = false)
    private double threshold;

    @Column(nullable = false)
    private boolean enabled = true;

    protected Subscription() {
    }

    public Subscription(AppUser user, Location location, RuleType ruleType, double threshold) {
        this.user = user;
        this.location = location;
        this.ruleType = ruleType;
        this.threshold = threshold;
    }

    public UUID getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Location getLocation() {
        return location;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public double getThreshold() {
        return threshold;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void disable() {
        this.enabled = false;
    }
}
