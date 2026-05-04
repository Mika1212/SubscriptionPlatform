package org.mika1212.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceCreatedEvent(
        UUID invoiceId,
        UUID userId,
        BigDecimal amount,
        LocalDate billingDate
) {}
