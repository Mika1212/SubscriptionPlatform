package org.mika1212.cache.entity;

import org.mika1212.common.entity.SubscriptionStatus;
import org.mika1212.common.entity.SubscriptionType;

import java.time.LocalDate;
import java.util.UUID;

public record SubscriptionView(
        UUID userId,
        UUID subscriptionId,
        SubscriptionType type,
        LocalDate activationDate,
        SubscriptionStatus status
) {}
