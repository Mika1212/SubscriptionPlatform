package org.mika1212.subscription.dto;

import org.mika1212.subscription.entity.SubscriptionType;

import java.time.LocalDate;
import java.util.UUID;

public record UserSubscriptionDeactivatedEvent(
        UUID userId,
        SubscriptionType type
) {}
