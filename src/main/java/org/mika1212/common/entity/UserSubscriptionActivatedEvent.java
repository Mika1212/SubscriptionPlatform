package org.mika1212.common.entity;

import java.time.LocalDate;
import java.util.UUID;

public record UserSubscriptionActivatedEvent(
        UUID userId,
        SubscriptionType type,
        LocalDate activationDate
) {}
