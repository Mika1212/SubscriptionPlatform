package org.mika1212.subscription.messaging;

import org.mika1212.config.RabbitConfig;
import org.mika1212.subscription.entity.OutboxEventEntity;
import org.mika1212.subscription.entity.OutboxEventStatus;
import org.mika1212.subscription.repository.OutboxRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxProcessor {

    private final OutboxRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxProcessor(OutboxRepository repository,
                           RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void process() {

        List<OutboxEventEntity> events =
                repository.findAllByStatus(OutboxEventStatus.NEW);

        for (OutboxEventEntity event : events) {
            try {
                rabbitTemplate.convertAndSend(
                        RabbitConfig.INVOICE_QUEUE,
                        event.getPayload()
                );

                event.setStatus(OutboxEventStatus.SENT);
                repository.save(event);

            } catch (Exception e) {
                event.setStatus(OutboxEventStatus.FAILED);
                repository.save(event);
            }
        }
    }
}
