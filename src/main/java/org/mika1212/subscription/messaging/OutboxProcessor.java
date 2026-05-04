package org.mika1212.subscription.messaging;

import org.mika1212.subscription.entity.OutboxEventEntity;
import org.mika1212.subscription.entity.OutboxEventStatus;
import org.mika1212.subscription.repository.OutboxRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxProcessor {

    private final OutboxRepository repository;
    private final RabbitEventPublisher eventPublisher;

    public OutboxProcessor(OutboxRepository repository, RabbitEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedDelay = 5000)
    public void process() {

        List<OutboxEventEntity> events =
                repository.findTop50ByStatusOrderByCreatedAtAsc(OutboxEventStatus.NEW);

        for (OutboxEventEntity event : events) {
            try {
                eventPublisher.publish(event.getEventType(), event.getPayload());

                event.setStatus(OutboxEventStatus.SENT);
                repository.save(event);

            } catch (Exception e) {

                event.setRetryCount(event.getRetryCount() + 1);

                if (event.getRetryCount() < 5) {
                    event.setStatus(OutboxEventStatus.NEW);
                } else {
                    // TODO dead letter queue
                    event.setStatus(OutboxEventStatus.FAILED);
                }

                repository.save(event);
            }
        }
    }
}
