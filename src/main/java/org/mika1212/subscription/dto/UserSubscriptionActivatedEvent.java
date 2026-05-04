package org.mika1212.subscription.dto;

import org.mika1212.subscription.entity.SubscriptionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UserSubscriptionActivatedEvent(
        UUID userId,
        SubscriptionType type,
        LocalDate activationDate
) {}
