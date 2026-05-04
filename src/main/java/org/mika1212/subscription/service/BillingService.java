package org.mika1212.subscription.service;

import org.mika1212.subscription.entity.*;
import org.mika1212.subscription.properties.BillingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);
    private final BatchingService batchingService;
    private final BillingProperties billingProperties;

    public BillingService(BatchingService batchingService, BillingProperties billingProperties) {
        this.batchingService = batchingService;
        this.billingProperties = billingProperties;
    }

    public void generateInvoices() {

        LocalDate today = LocalDate.now();

        if (billingProperties.getBatchSize() <= 0) {
            throw new IllegalStateException("batch-size must be > 0");
        }

        int batchSize = billingProperties.getBatchSize();

        log.info("Billing started, date={}, batchSize={}", today, batchSize);

        while (true) {

            List<SubscriptionEntity> batch =
                    batchingService.processBatch(today, batchSize);

            if (batch.isEmpty()) {
                log.info("No more subscriptions to process");
                break;
            }
        }

        log.info("Billing finished");
    }
}
