package org.mika1212.subscription.service;

import org.mika1212.common.json.JacksonJsonSerializer;
import org.mika1212.subscription.dto.*;
import org.mika1212.subscription.exception.SubscriptionActivateDateException;
import org.mika1212.subscription.exception.SubscriptionAlreadyExistsException;
import org.mika1212.subscription.exception.SubscriptionNotFoundException;
import org.mika1212.subscription.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.mika1212.subscription.entity.*;

import org.mika1212.subscription.repository.SubscriptionRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionRepository repository;
    private final OutboxRepository outboxRepository;
    private final JacksonJsonSerializer jsonSerializer;

    public SubscriptionService(SubscriptionRepository repository, OutboxRepository outboxRepository, JacksonJsonSerializer jsonSerializer) {
        this.repository = repository;
        this.outboxRepository = outboxRepository;
        this.jsonSerializer = jsonSerializer;
    }

    @Transactional
    public ActivateSubscriptionResponse activate(ActivateSubscriptionDTO subscriptionDTO) {

        validateActivationDate(subscriptionDTO.activationDate());

        repository.findByUserIdAndStatus(subscriptionDTO.userId(), SubscriptionStatus.ACTIVE)
                .ifPresent(s -> {
                    throw new SubscriptionAlreadyExistsException("User already has active subscription");
                });

        SubscriptionEntity entity = buildActiveSubscriptionEntity(subscriptionDTO);

        repository.save(entity);

        UserSubscriptionActivatedEvent event = new UserSubscriptionActivatedEvent(
                entity.getUserId(),
                entity.getType(),
                entity.getActivationDate()
        );

        outboxRepository.save(
                OutboxEventEntity.builder()
                        .id(UUID.randomUUID())
                        .eventType(OutboxEventType.SUBSCRIPTION_ACTIVATED)
                        .payload(jsonSerializer.toJson(event))
                        .status(OutboxEventStatus.NEW)
                        .retryCount(0)
                        .createdAt(Instant.now())
                        .build()
        );
        return new ActivateSubscriptionResponse("OK", "Subscription activated");
    }

    @Transactional
    public DeactivateSubscriptionResponse deactivate(DeactivateSubscriptionDTO subscriptionDTO) {

        SubscriptionEntity subscription = repository
                .findByUserIdAndStatus(subscriptionDTO.userId(), SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new SubscriptionNotFoundException("No active subscription found"));

        subscription.setStatus(SubscriptionStatus.INACTIVE);
        subscription.setDeactivationDate(LocalDate.now());

        repository.save(subscription);

        UserSubscriptionDeactivatedEvent event =
                new UserSubscriptionDeactivatedEvent(
                        subscription.getUserId(),
                        subscription.getType()
                );

        outboxRepository.save(
                OutboxEventEntity.builder()
                        .id(UUID.randomUUID())
                        .eventType(OutboxEventType.SUBSCRIPTION_DEACTIVATED)
                        .payload(jsonSerializer.toJson(event))
                        .status(OutboxEventStatus.NEW)
                        .createdAt(Instant.now())
                        .retryCount(0)
                        .build()
        );

        return new DeactivateSubscriptionResponse("OK", "Subscription deactivated");
    }

    private void validateActivationDate(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new SubscriptionActivateDateException("Activation date cannot be in the past");
        }
    }

    private SubscriptionEntity buildActiveSubscriptionEntity(ActivateSubscriptionDTO subscriptionDTO) {
        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setUserId(subscriptionDTO.userId());
        entity.setType(subscriptionDTO.type());
        entity.setStatus(SubscriptionStatus.ACTIVE);
        entity.setActivationDate(subscriptionDTO.activationDate());

        return entity;
    }
}
