package org.mika1212.subscription.e2e;

import org.junit.jupiter.api.Test;
import org.mika1212.subscription.entity.*;
import org.mika1212.subscription.repository.InvoiceRepository;
import org.mika1212.subscription.repository.OutboxRepository;
import org.mika1212.subscription.repository.SubscriptionRepository;
import org.mika1212.subscription.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class BillingE2ETest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private BillingService billingService;

    @Test
    void shouldGenerateInvoice_andOutbox_andUpdateSubscription() {

        LocalDate today = LocalDate.now();
        UUID userId = UUID.randomUUID();

        SubscriptionEntity sub = new SubscriptionEntity();
        sub.setUserId(userId);
        sub.setType(SubscriptionType.BASIC);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setNextBillingDate(today);
        sub.setActivationDate(today);

        sub.setBillingDayOfMonth(10);

        SubscriptionEntity saveSub = subscriptionRepository.save(sub);

        billingService.generateInvoices();

        // 1. invoice created
        List<InvoiceEntity> invoices = invoiceRepository.findAll();

        assertThat(invoices).hasSize(1);

        InvoiceEntity invoice = invoices.get(0);

        assertThat(invoice.getUserId()).isEqualTo(userId);
        assertThat(invoice.getSubscriptionId()).isEqualTo(saveSub.getId());
        assertThat(invoice.getAmount()).isEqualTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP));

        // 2. outbox event created
        List<OutboxEventEntity> events = outboxRepository.findAll();

        assertThat(events).hasSize(1);

        OutboxEventEntity event = events.get(0);

        assertThat(event.getEventType())
                .isEqualTo(OutboxEventType.INVOICE_CREATED);

        // 3. subscription updated
        SubscriptionEntity updated =
                subscriptionRepository.findById(saveSub.getId()).orElseThrow();

        assertThat(updated.getNextBillingDate()).isNotNull();
    }
}
