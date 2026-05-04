package org.mika1212.cache.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceView(
        UUID invoiceId,
        BigDecimal amount,
        LocalDate billingDate
) {}
