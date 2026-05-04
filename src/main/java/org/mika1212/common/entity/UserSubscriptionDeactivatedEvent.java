package org.mika1212.common.entity;

import java.util.UUID;

public record UserSubscriptionDeactivatedEvent(
        UUID userId,
        UUID subscriptionId,
        SubscriptionType type
) {}
