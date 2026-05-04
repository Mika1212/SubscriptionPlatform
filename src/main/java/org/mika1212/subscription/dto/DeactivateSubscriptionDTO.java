package org.mika1212.subscription.dto;

import org.mika1212.common.entity.SubscriptionType;

import java.util.UUID;

public record DeactivateSubscriptionDTO(
        UUID userId,
        SubscriptionType type
) {}
