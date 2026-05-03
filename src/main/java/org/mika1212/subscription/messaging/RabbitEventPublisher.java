package org.mika1212.subscription.messaging;

import org.mika1212.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(String message) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.INVOICE_QUEUE,
                message
        );
    }
}