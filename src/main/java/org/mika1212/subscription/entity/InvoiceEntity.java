package org.mika1212.subscription.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "invoices")
public class InvoiceEntity {

    @Id
    private UUID id;

    private UUID userId;

    @Column(nullable = false)
    private UUID subscriptionId;

    @Column(nullable = false)
    private LocalDate billingDate;

    @Enumerated(EnumType.STRING)
    private SubscriptionType subscriptionType;

    private int amount;
}
