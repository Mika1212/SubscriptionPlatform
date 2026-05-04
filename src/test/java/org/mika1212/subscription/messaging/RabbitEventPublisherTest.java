package org.mika1212.subscription.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mika1212.config.RabbitConfig;
import org.mika1212.subscription.entity.OutboxEventType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitEventPublisher publisher;

    @Test
    void publish_shouldSendToInvoiceQueue_whenInvoiceEvent() {

        publisher.publish(OutboxEventType.INVOICE_CREATED, "payload");

        verify(rabbitTemplate).convertAndSend(
                RabbitConfig.INVOICE_QUEUE,
                "payload"
        );
    }

    @Test
    void publish_shouldSendToSubscriptionQueue_whenSubscriptionActivated() {

        publisher.publish(OutboxEventType.SUBSCRIPTION_ACTIVATED, "payload");

        verify(rabbitTemplate).convertAndSend(
                RabbitConfig.SUBSCRIPTION_QUEUE,
                "payload"
        );
    }

    @Test
    void publish_shouldSendToSubscriptionQueue_whenSubscriptionDeactivated() {

        publisher.publish(OutboxEventType.SUBSCRIPTION_DEACTIVATED, "payload");

        verify(rabbitTemplate).convertAndSend(
                RabbitConfig.UNSUBSCRIPTION_QUEUE,
                "payload"
        );
    }
}