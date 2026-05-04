package org.mika1212.subscription.scheduler;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.mika1212.subscription.service.BillingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BillingScheduler {

    private final BillingService billingService;

    public BillingScheduler(BillingService billingService) {
        this.billingService = billingService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "billingJob")
    public void runBilling() {
        billingService.generateInvoices();
    }
}
