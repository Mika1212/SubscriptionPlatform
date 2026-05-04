package org.mika1212.subscription.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mika1212.subscription.service.BillingService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BillingSchedulerTest {

    @Mock
    private BillingService billingService;

    @InjectMocks
    private BillingScheduler scheduler;

    @Test
    void runBilling_shouldCallBillingService() {

        scheduler.runBilling();

        verify(billingService).generateInvoices();
    }
}