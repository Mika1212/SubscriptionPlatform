package org.mika1212.subscription.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mika1212.subscription.entity.SubscriptionEntity;
import org.mika1212.subscription.repository.SubscriptionRepository;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchingServiceTest {
    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionBillingProcessor processor;

    private final Executor executor = Runnable::run;

    private BatchingService service;

    @BeforeEach
    void setUp() {
        service = new BatchingService(subscriptionRepository, processor, executor);
    }

    @Test
    void processBatch_shouldProcessAllSubscriptions() {

        LocalDate today = LocalDate.now();

        SubscriptionEntity s1 = new SubscriptionEntity();
        s1.setId(UUID.randomUUID());

        SubscriptionEntity s2 = new SubscriptionEntity();
        s2.setId(UUID.randomUUID());

        List<SubscriptionEntity> batch = List.of(s1, s2);

        when(subscriptionRepository.claimBatch(today, 10))
                .thenReturn(batch);

        var result = service.processBatch(today, 10);

        verify(processor).process(s1.getId(), today);
        verify(processor).process(s2.getId(), today);

        assertThat(result)
                .extracting(SubscriptionEntity::getId)
                .containsExactlyInAnyOrder(s1.getId(), s2.getId());
    }

    @Test
    void processBatch_shouldHandleEmptyBatch() {

        LocalDate today = LocalDate.now();

        when(subscriptionRepository.claimBatch(today, 10))
                .thenReturn(List.of());

        var result = service.processBatch(today, 10);

        verifyNoInteractions(processor);

        assertThat(result).isEmpty();
    }

    @Test
    void processBatch_shouldContinue_whenProcessorFails() {

        LocalDate today = LocalDate.now();

        SubscriptionEntity s1 = new SubscriptionEntity();
        s1.setId(UUID.randomUUID());

        SubscriptionEntity s2 = new SubscriptionEntity();
        s2.setId(UUID.randomUUID());

        List<SubscriptionEntity> batch = List.of(s1, s2);

        when(subscriptionRepository.claimBatch(today, 10))
                .thenReturn(batch);

        doThrow(new RuntimeException("fail"))
                .when(processor).process(s1.getId(), today);

        service.processBatch(today, 10);

        verify(processor).process(s1.getId(), today);
        verify(processor).process(s2.getId(), today);
    }
}