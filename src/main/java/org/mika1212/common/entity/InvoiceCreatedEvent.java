package org.mika1212.common.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceCreatedEvent(
        UUID invoiceId,
        UUID userId,
        BigDecimal amount,
        LocalDate billingDate
) {}
