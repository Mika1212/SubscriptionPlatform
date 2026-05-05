package org.mika1212.cache.entity;

import java.util.List;

public record UserCacheResponse(
        List<SubscriptionView> subscriptions,
        List<InvoiceView> invoices,
        int page,
        int size,
        long totalInvoices
) {}
