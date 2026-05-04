package org.mika1212.subscription.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "subscriptions")
public class SubscriptionEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private SubscriptionType type;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private LocalDate activationDate;

    private LocalDate deactivationDate;

    @Column(nullable = false)
    private Integer billingDayOfMonth;

    @Column(nullable = false)
    private LocalDate nextBillingDate;

    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;

        if (billingDayOfMonth == null && activationDate != null) {
            billingDayOfMonth = activationDate.getDayOfMonth();
        }

        if (nextBillingDate == null) {
            nextBillingDate = activationDate;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
