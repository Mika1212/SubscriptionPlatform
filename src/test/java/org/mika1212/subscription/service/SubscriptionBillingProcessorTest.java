package org.mika1212.subscription.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mika1212.common.entity.InvoiceEntity;
import org.mika1212.common.entity.SubscriptionEntity;
import org.mika1212.common.entity.SubscriptionStatus;
import org.mika1212.common.entity.SubscriptionType;
import org.mika1212.common.json.JacksonJsonSerializer;
import org.mika1212.subscription.entity.*;
import org.mika1212.subscription.properties.SubscriptionPriceProperties;
import org.mika1212.subscription.repository.InvoiceRepository;
import org.mika1212.subscription.repository.OutboxRepository;
import org.mika1212.subscription.repository.SubscriptionRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionBillingProcessorTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private SubscriptionPriceProperties prices;

    @Mock
    private JacksonJsonSerializer jsonSerializer;

    @InjectMocks
    private SubscriptionBillingProcessor processor;

    @Test
    void process_shouldCreateInvoice_andOutbox_andUpdateSubscription() {

        UUID subId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.of(2026, 10, 8);

        SubscriptionEntity sub = new SubscriptionEntity();
        sub.setId(subId);
        sub.setUserId(userId);
        sub.setType(SubscriptionType.BASIC);
        sub.setNextBillingDate(today);
        sub.setBillingDayOfMonth(8);
        sub.setStatus(SubscriptionStatus.IN_PROGRESS);

        when(subscriptionRepository.findById(subId))
                .thenReturn(Optional.of(sub));

        when(prices.getBasic()).thenReturn(BigDecimal.valueOf(100));
        when(jsonSerializer.toJson(any())).thenReturn("json");

        ArgumentCaptor<InvoiceEntity> invoiceCaptor =
                ArgumentCaptor.forClass(InvoiceEntity.class);

        ArgumentCaptor<OutboxEventEntity> outboxCaptor =
                ArgumentCaptor.forClass(OutboxEventEntity.class);

        processor.process(subId, today);

        verify(invoiceRepository).save(invoiceCaptor.capture());
        verify(outboxRepository).save(outboxCaptor.capture());
        verify(subscriptionRepository).save(sub);

        InvoiceEntity invoice = invoiceCaptor.getValue();

        assertThat(invoice.getUserId()).isEqualTo(userId);
        assertThat(invoice.getSubscriptionId()).isEqualTo(subId);
        assertThat(invoice.getAmount()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(invoice.getBillingDate()).isEqualTo(today);

        OutboxEventEntity event = outboxCaptor.getValue();

        assertThat(event.getEventType()).isEqualTo(OutboxEventType.INVOICE_CREATED);
        assertThat(event.getPayload()).isEqualTo("json");

        assertThat(sub.getNextBillingDate()).isEqualTo(LocalDate.of(2026, 11, 8));
        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void process_shouldThrow_whenSubscriptionNotFound() {

        UUID subId = UUID.randomUUID();

        when(subscriptionRepository.findById(subId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> processor.process(subId, LocalDate.now()))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(invoiceRepository);
        verifyNoInteractions(outboxRepository);
    }

    @Test
    void process_shouldReturnEarly_whenDuplicateInvoice() {

        UUID subId = UUID.randomUUID();

        SubscriptionEntity sub = new SubscriptionEntity();
        sub.setId(subId);
        sub.setType(SubscriptionType.BASIC);

        when(subscriptionRepository.findById(subId))
                .thenReturn(Optional.of(sub));

        when(prices.getBasic()).thenReturn(BigDecimal.valueOf(100));

        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(invoiceRepository).save(any());

        processor.process(subId, LocalDate.now());

        verify(outboxRepository, never()).save(any());
        verify(subscriptionRepository, never()).save(sub);
    }

    @Test
    void process_shouldUseProPrice() {

        UUID subId = UUID.randomUUID();

        SubscriptionEntity sub = new SubscriptionEntity();
        sub.setId(subId);
        sub.setType(SubscriptionType.PRO);
        sub.setNextBillingDate(LocalDate.now());
        sub.setBillingDayOfMonth(10);

        when(subscriptionRepository.findById(subId))
                .thenReturn(Optional.of(sub));

        when(prices.getPro()).thenReturn(BigDecimal.valueOf(200));
        when(jsonSerializer.toJson(any())).thenReturn("json");

        ArgumentCaptor<InvoiceEntity> captor =
                ArgumentCaptor.forClass(InvoiceEntity.class);

        processor.process(subId, LocalDate.now());

        verify(invoiceRepository).save(captor.capture());

        assertThat(captor.getValue().getAmount())
                .isEqualTo(BigDecimal.valueOf(200));
    }
}