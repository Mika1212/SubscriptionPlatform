package org.mika1212.subscription.messaging;

import org.mika1212.config.RabbitConfig;
import org.mika1212.subscription.entity.OutboxEventType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(OutboxEventType type, String message) {

        String queue = switch (type) {
            case INVOICE_CREATED -> RabbitConfig.INVOICE_QUEUE;
            case SUBSCRIPTION_ACTIVATED,
                 SUBSCRIPTION_DEACTIVATED -> RabbitConfig.SUBSCRIPTION_QUEUE;
        };

        rabbitTemplate.convertAndSend(queue, message);
    }
}
