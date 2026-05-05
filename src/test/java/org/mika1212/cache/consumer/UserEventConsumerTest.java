package org.mika1212.cache.consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mika1212.cache.service.UserCacheService;
import org.mika1212.common.entity.InvoiceCreatedEvent;
import org.mika1212.common.entity.SubscriptionType;
import org.mika1212.common.entity.UserSubscriptionActivatedEvent;
import org.mika1212.common.entity.UserSubscriptionDeactivatedEvent;
import org.mika1212.common.json.JacksonJsonSerializer;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserEventConsumerTest {

    @Mock
    private UserCacheService cacheService;

    @Mock
    private JacksonJsonSerializer serializer;

    @InjectMocks
    private UserEventConsumer consumer;

    @Test
    void should_handle_subscription_event() {
        String msg = "{}";

        UserSubscriptionActivatedEvent event =
                new UserSubscriptionActivatedEvent(UUID.randomUUID(), UUID.randomUUID(), SubscriptionType.BASIC, LocalDate.now());

        when(serializer.toObject(any(), eq(UserSubscriptionActivatedEvent.class)))
                .thenReturn(event);

        consumer.handleSubscriptionActivated(msg);

        verify(cacheService).applySubscriptionEvent(event);
    }

    @Test
    void should_handle_subscription_deactivation_event() {
        String msg = "{}";

        UserSubscriptionDeactivatedEvent event =
                new UserSubscriptionDeactivatedEvent(UUID.randomUUID(), UUID.randomUUID(), SubscriptionType.BASIC);

        when(serializer.toObject(any(), eq(UserSubscriptionDeactivatedEvent.class)))
                .thenReturn(event);

        consumer.handleSubscriptionDeactivated(msg);

        verify(cacheService).applySubscriptionEvent(event);
    }

    @Test
    void should_handle_invoice_event() {
        String msg = "{}";

        InvoiceCreatedEvent event =
                new InvoiceCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(100), LocalDate.now());

        when(serializer.toObject(any(), eq(InvoiceCreatedEvent.class)))
                .thenReturn(event);

        consumer.handleInvoiceEvents(msg);

        verify(cacheService).applyInvoiceEvent(event);
    }
}
