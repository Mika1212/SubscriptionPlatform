package org.mika1212.subscription.service;

import org.mika1212.common.json.JacksonJsonSerializer;
import org.mika1212.subscription.dto.InvoiceCreatedEvent;
import org.mika1212.subscription.entity.*;
import org.mika1212.subscription.repository.InvoiceRepository;
import org.mika1212.subscription.repository.OutboxRepository;
import org.mika1212.subscription.repository.SubscriptionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class SubscriptionBillingProcessor {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final OutboxRepository outboxRepository;
    private final JacksonJsonSerializer jsonService;

    public SubscriptionBillingProcessor(SubscriptionRepository subscriptionRepository, InvoiceRepository invoiceRepository, OutboxRepository outboxRepository, JacksonJsonSerializer jsonService) {
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.outboxRepository = outboxRepository;
        this.jsonService = jsonService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(UUID subscriptionId, LocalDate today) {
        SubscriptionEntity sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalStateException(
                        "Subscription not found: " + subscriptionId
                ));

        int amount = calculateAmount(sub.getType());

        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setId(UUID.randomUUID());
        invoice.setUserId(sub.getUserId());
        invoice.setSubscriptionId(sub.getId());
        invoice.setSubscriptionType(sub.getType());
        invoice.setAmount(amount);
        invoice.setBillingDate(today);

        try {
            invoiceRepository.save(invoice);
        } catch (DataIntegrityViolationException e) {
            return;
        }

        outboxRepository.save(
                OutboxEventEntity.builder()
                        .id(UUID.randomUUID())
                        .eventType(OutboxEventType.INVOICE_CREATED)
                        .payload(jsonService.toJson(new InvoiceCreatedEvent(
                                invoice.getId(),
                                invoice.getUserId(),
                                invoice.getAmount(),
                                invoice.getBillingDate()
                        )))
                        .status(OutboxEventStatus.NEW)
                        .createdAt(Instant.now())
                        .build()
        );

        sub.setNextBillingDate(
                calculateNextBillingDate(
                        sub.getNextBillingDate(),
                        sub.getBillingDayOfMonth()
                )
        );
    }

    private LocalDate calculateNextBillingDate(LocalDate from, int billingDayOfMonth) {

        LocalDate next = from.plusMonths(1);

        int lastDayOfMonth = next.lengthOfMonth();

        int day = Math.min(billingDayOfMonth, lastDayOfMonth);

        return LocalDate.of(next.getYear(), next.getMonth(), day);
    }

    private int calculateAmount(SubscriptionType type) {
        return switch (type) {
            case BASIC -> 100;
            case PRO -> 200;
        };
    }
}
