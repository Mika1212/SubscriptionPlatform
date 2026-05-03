package org.mika1212.subscription.dto;

import java.time.LocalDate;
import java.util.UUID;

public record InvoiceCreatedEvent(
        UUID invoiceId,
        UUID userId,
        int amount,
        LocalDate billingDate
) {}
