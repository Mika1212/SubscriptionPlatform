package org.mika1212.subscription.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mika1212.common.entity.SubscriptionEntity;
import org.mika1212.common.entity.SubscriptionStatus;
import org.mika1212.common.entity.SubscriptionType;
import org.mika1212.common.json.JacksonJsonSerializer;
import org.mika1212.subscription.dto.ActivateSubscriptionDTO;
import org.mika1212.subscription.dto.DeactivateSubscriptionDTO;
import org.mika1212.subscription.entity.*;
import org.mika1212.subscription.exception.SubscriptionActivateDateException;
import org.mika1212.subscription.exception.SubscriptionAlreadyExistsException;
import org.mika1212.subscription.exception.SubscriptionNotFoundException;
import org.mika1212.subscription.repository.OutboxRepository;
import org.mika1212.subscription.repository.SubscriptionRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository repository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private JacksonJsonSerializer jsonSerializer;

    @InjectMocks
    private SubscriptionService service;

    @Test
    void activate_success() {
        UUID userId = UUID.randomUUID();
        LocalDate activationDate = LocalDate.now().plusDays(1);

        ActivateSubscriptionDTO dto =
                new ActivateSubscriptionDTO(
                        userId,
                        SubscriptionType.BASIC,
                        activationDate
                );

        when(repository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        when(jsonSerializer.toJson(any())).thenReturn("json");

        ArgumentCaptor<SubscriptionEntity> captor =
                ArgumentCaptor.forClass(SubscriptionEntity.class);

        ArgumentCaptor<OutboxEventEntity> outboxCaptor =
                ArgumentCaptor.forClass(OutboxEventEntity.class);

        var response = service.activate(dto);

        assertThat(response.status()).isEqualTo("OK");

        verify(repository).save(captor.capture());
        verify(outboxRepository).save(outboxCaptor.capture());

        SubscriptionEntity saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getType()).isEqualTo(SubscriptionType.BASIC);
        assertThat(saved.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(saved.getActivationDate()).isEqualTo(activationDate);

        OutboxEventEntity savedEvent = outboxCaptor.getValue();

        assertThat(savedEvent.getId()).isNotNull();
        assertThat(savedEvent.getStatus()).isEqualTo(OutboxEventStatus.NEW);
        assertThat(savedEvent.getEventType()).isEqualTo(OutboxEventType.SUBSCRIPTION_ACTIVATED);
        assertThat(savedEvent.getPayload()).isEqualTo("json");
        assertThat(savedEvent.getCreatedAt()).isNotNull();
    }

    @Test
    void activate_shouldThrow_whenAlreadyExists() {
        UUID userId = UUID.randomUUID();

        when(repository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(new SubscriptionEntity()));

        ActivateSubscriptionDTO dto =
                new ActivateSubscriptionDTO(
                        userId,
                        SubscriptionType.BASIC,
                        LocalDate.now().plusDays(1)
                );

        assertThatThrownBy(() -> service.activate(dto))
                .isInstanceOf(SubscriptionAlreadyExistsException.class);
    }

    @Test
    void activate_shouldThrow_whenDateInPast() {
        ActivateSubscriptionDTO dto =
                new ActivateSubscriptionDTO(
                        UUID.randomUUID(),
                        SubscriptionType.BASIC,
                        LocalDate.now().minusDays(1)
                );

        assertThatThrownBy(() -> service.activate(dto))
                .isInstanceOf(SubscriptionActivateDateException.class);
    }

    @Test
    void deactivate_success() {
        UUID userId = UUID.randomUUID();

        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setStatus(SubscriptionStatus.ACTIVE);

        when(repository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(entity));

        when(jsonSerializer.toJson(any())).thenReturn("json");

        DeactivateSubscriptionDTO dto =
                new DeactivateSubscriptionDTO(userId, SubscriptionType.BASIC);

        ArgumentCaptor<SubscriptionEntity> captor =
                ArgumentCaptor.forClass(SubscriptionEntity.class);

        ArgumentCaptor<OutboxEventEntity> outboxCaptor =
                ArgumentCaptor.forClass(OutboxEventEntity.class);

        var response = service.deactivate(dto);

        assertThat(response.status()).isEqualTo("OK");

        verify(repository).save(captor.capture());
        verify(outboxRepository).save(outboxCaptor.capture());

        SubscriptionEntity saved = captor.getValue();

        LocalDate today = LocalDate.now();
        assertThat(saved.getStatus()).isEqualTo(SubscriptionStatus.INACTIVE);
        assertThat(saved.getDeactivationDate()).isEqualTo(today);

        OutboxEventEntity savedEvent = outboxCaptor.getValue();

        assertThat(savedEvent.getId()).isNotNull();
        assertThat(savedEvent.getStatus()).isEqualTo(OutboxEventStatus.NEW);
        assertThat(savedEvent.getEventType()).isEqualTo(OutboxEventType.SUBSCRIPTION_DEACTIVATED);
        assertThat(savedEvent.getPayload()).isEqualTo("json");
        assertThat(savedEvent.getCreatedAt()).isNotNull();
        assertThat(savedEvent.getRetryCount()).isEqualTo(0);
    }

    @Test
    void deactivate_shouldThrow_whenNotFound() {
        UUID userId = UUID.randomUUID();

        when(repository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        DeactivateSubscriptionDTO dto =
                new DeactivateSubscriptionDTO(userId, SubscriptionType.BASIC);

        assertThatThrownBy(() -> service.deactivate(dto))
                .isInstanceOf(SubscriptionNotFoundException.class);
    }
}
