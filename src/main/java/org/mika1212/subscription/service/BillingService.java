package org.mika1212.subscription.service;

import org.mika1212.subscription.entity.*;
import org.mika1212.subscription.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);
    private final BatchingService batchingService;

    public BillingService(BatchingService batchingService) {
        this.batchingService = batchingService;
    }

    public void generateInvoices() {

        LocalDate today = LocalDate.now();
        int batchSize = 100;

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
