package org.mika1212.subscription.service;

import org.mika1212.subscription.entity.SubscriptionEntity;
import org.mika1212.subscription.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
public class BatchingService {

    private static final Logger log = LoggerFactory.getLogger(BatchingService.class);
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionBillingProcessor processor;
    private final Executor billingExecutor;

    public BatchingService(
            SubscriptionRepository subscriptionRepository,
            SubscriptionBillingProcessor processor,
            Executor billingExecutor
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.processor = processor;
        this.billingExecutor = billingExecutor;
    }

    @Transactional
    public List<SubscriptionEntity> processBatch(LocalDate today, int batchSize) {

        List<SubscriptionEntity> batch =
                subscriptionRepository.lockBatch(today, batchSize);

        log.info("Locked batch size={}", batch.size());

        List<CompletableFuture<Void>> futures = batch.stream()
                .map(sub -> CompletableFuture.runAsync(() -> {
                    try {
                        processor.process(sub.getId(), today);
                    } catch (Exception e) {
                        log.error("Failed subscription {}", sub.getId(), e);
                    }
                }, billingExecutor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(30, TimeUnit.SECONDS)
                .join();

        log.info("Processed batch size={}", batch.size());

        return batch;
    }
}
