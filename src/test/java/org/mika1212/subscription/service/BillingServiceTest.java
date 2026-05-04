package org.mika1212.subscription.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mika1212.common.entity.SubscriptionEntity;
import org.mika1212.subscription.properties.BillingProperties;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private BatchingService batchingService;

    @Mock
    private BillingProperties billingProperties;

    @InjectMocks
    private BillingService billingService;

    @Test
    void generateInvoices_shouldThrow_whenBatchSizeInvalid() {

        when(billingProperties.getBatchSize()).thenReturn(0);

        assertThatThrownBy(() -> billingService.generateInvoices())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void generateInvoices_shouldStop_whenBatchIsEmpty() {

        when(billingProperties.getBatchSize()).thenReturn(10);

        when(batchingService.processBatch(any(), anyInt()))
                .thenReturn(List.of());

        billingService.generateInvoices();

        verify(batchingService, times(1)).processBatch(any(), eq(10));
    }

    @Test
    void generateInvoices_shouldProcessMultipleBatches_untilEmpty() {

        when(billingProperties.getBatchSize()).thenReturn(10);

        SubscriptionEntity s1 = new SubscriptionEntity();
        SubscriptionEntity s2 = new SubscriptionEntity();

        when(batchingService.processBatch(any(), anyInt()))
                .thenReturn(List.of(s1, s2))
                .thenReturn(List.of());

        billingService.generateInvoices();

        verify(batchingService, times(2))
                .processBatch(any(), eq(10));
    }
}