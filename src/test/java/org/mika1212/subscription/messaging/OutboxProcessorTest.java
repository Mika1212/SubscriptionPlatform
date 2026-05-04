package org.mika1212.subscription.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mika1212.subscription.entity.OutboxEventEntity;
import org.mika1212.subscription.entity.OutboxEventStatus;
import org.mika1212.subscription.entity.OutboxEventType;
import org.mika1212.subscription.repository.OutboxRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxProcessorTest {

    @Mock
    private OutboxRepository repository;

    @Mock
    private RabbitEventPublisher eventPublisher;

    @InjectMocks
    private OutboxProcessor processor;

    @Test
    void process_shouldSendEvent_andMarkAsSent() {

        OutboxEventEntity event = new OutboxEventEntity();
        event.setEventType(OutboxEventType.SUBSCRIPTION_ACTIVATED);
        event.setPayload("json");
        event.setStatus(OutboxEventStatus.NEW);
        event.setRetryCount(0);

        when(repository.findTop50ByStatusOrderByCreatedAtAsc(OutboxEventStatus.NEW))
                .thenReturn(List.of(event));

        processor.process();

        verify(eventPublisher).publish(
                OutboxEventType.SUBSCRIPTION_ACTIVATED,
                "json"
        );

        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.SENT);

        verify(repository).save(event);
    }

    @Test
    void process_shouldRetry_whenPublishFails_andRetryLessThan5() {

        OutboxEventEntity event = new OutboxEventEntity();
        event.setEventType(OutboxEventType.SUBSCRIPTION_ACTIVATED);
        event.setPayload("json");
        event.setStatus(OutboxEventStatus.NEW);
        event.setRetryCount(2);

        when(repository.findTop50ByStatusOrderByCreatedAtAsc(OutboxEventStatus.NEW))
                .thenReturn(List.of(event));

        doThrow(new RuntimeException("Rabbit down"))
                .when(eventPublisher)
                .publish(any(), any());

        processor.process();

        assertThat(event.getRetryCount()).isEqualTo(3);
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.NEW);

        verify(repository).save(event);
    }

    @Test
    void process_shouldMarkFailed_whenRetryLimitExceeded() {

        OutboxEventEntity event = new OutboxEventEntity();
        event.setEventType(OutboxEventType.SUBSCRIPTION_ACTIVATED);
        event.setPayload("json");
        event.setStatus(OutboxEventStatus.NEW);
        event.setRetryCount(5);

        when(repository.findTop50ByStatusOrderByCreatedAtAsc(OutboxEventStatus.NEW))
                .thenReturn(List.of(event));

        doThrow(new RuntimeException("Rabbit down"))
                .when(eventPublisher)
                .publish(any(), any());

        processor.process();

        assertThat(event.getRetryCount()).isEqualTo(6);
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);

        verify(repository).save(event);
    }
}