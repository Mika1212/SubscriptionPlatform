package org.mika1212.subscription.dto;

import org.mika1212.common.entity.SubscriptionType;

import java.time.LocalDate;
import java.util.UUID;

public record ActivateSubscriptionDTO(
        UUID userId,
        SubscriptionType type,
        LocalDate activationDate
) {}
