package org.mika1212.subscription.service;

import org.mika1212.common.json.JacksonJsonSerializer;
import org.mika1212.subscription.properties.SubscriptionPriceProperties;
import org.mika1212.subscription.dto.InvoiceCreatedEvent;
import org.mika1212.subscription.entity.*;
import org.mika1212.subscription.repository.InvoiceRepository;
import org.mika1212.subscription.repository.OutboxRepository;
import org.mika1212.subscription.repository.SubscriptionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class SubscriptionBillingProcessor {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final OutboxRepository outboxRepository;
    private final SubscriptionPriceProperties prices;
    private final JacksonJsonSerializer jsonSerializer;


    public SubscriptionBillingProcessor(
            SubscriptionRepository subscriptionRepository,
            InvoiceRepository invoiceRepository,
            OutboxRepository outboxRepository,
            SubscriptionPriceProperties prices,
            JacksonJsonSerializer jsonSerializer) {
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.outboxRepository = outboxRepository;
        this.jsonSerializer = jsonSerializer;
        this.prices = prices;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(UUID subscriptionId, LocalDate today) {
        SubscriptionEntity sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalStateException(
                        "Subscription not found: " + subscriptionId
                ));

        InvoiceEntity invoice = buildInvoiceEntity(sub, calculateAmount(sub.getType()), today);

        try {
            invoiceRepository.save(invoice);
        } catch (DataIntegrityViolationException e) {
            return;
        }

        outboxRepository.save(
                OutboxEventEntity.builder()
                        .id(UUID.randomUUID())
                        .eventType(OutboxEventType.INVOICE_CREATED)
                        .payload(jsonSerializer.toJson(new InvoiceCreatedEvent(
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

        sub.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(sub);
    }

    private LocalDate calculateNextBillingDate(LocalDate from, int billingDayOfMonth) {

        LocalDate next = from.plusMonths(1);

        int lastDayOfMonth = next.lengthOfMonth();

        int day = Math.min(billingDayOfMonth, lastDayOfMonth);

        return LocalDate.of(next.getYear(), next.getMonth(), day);
    }

    private InvoiceEntity buildInvoiceEntity(SubscriptionEntity sub, BigDecimal amount, LocalDate billingDate) {
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setId(UUID.randomUUID());
        invoice.setUserId(sub.getUserId());
        invoice.setSubscriptionId(sub.getId());
        invoice.setSubscriptionType(sub.getType());
        invoice.setAmount(amount);
        invoice.setBillingDate(billingDate);

        return invoice;
    }

    private BigDecimal calculateAmount(SubscriptionType type) {
        return switch (type) {
            case BASIC -> requirePrice(prices.getBasic(), "BASIC");
            case PRO -> requirePrice(prices.getPro(), "PRO");
        };
    }

    private BigDecimal requirePrice(BigDecimal value, String type) {
        if (value == null) {
            throw new IllegalStateException("Price not configured for " + type);
        }
        return value;
    }
}
