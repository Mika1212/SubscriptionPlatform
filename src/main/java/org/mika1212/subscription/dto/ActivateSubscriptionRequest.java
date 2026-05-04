package org.mika1212.subscription.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import org.mika1212.subscription.entity.SubscriptionType;

import java.time.LocalDate;
import java.util.UUID;

public record ActivateSubscriptionRequest(
        @NotNull UUID userId,
        @NotNull SubscriptionType type,

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @NotNull LocalDate activationDate
) {}
