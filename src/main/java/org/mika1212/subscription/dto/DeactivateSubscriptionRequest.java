package org.mika1212.subscription.dto;

import jakarta.validation.constraints.NotNull;
import org.mika1212.subscription.entity.SubscriptionType;

import java.util.UUID;

public record DeactivateSubscriptionRequest(
        @NotNull UUID userId,
        @NotNull SubscriptionType type
) {}
