package org.mika1212.subscription.service;

import org.mika1212.subscription.dto.ActivateSubscriptionResponse;
import org.mika1212.subscription.dto.DeactivateSubscriptionResponse;
import org.mika1212.subscription.exception.SubscriptionActivateDateException;
import org.mika1212.subscription.exception.SubscriptionAlreadyExistsException;
import org.mika1212.subscription.exception.SubscriptionNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.mika1212.subscription.dto.ActivateSubscriptionRequest;
import org.mika1212.subscription.dto.DeactivateSubscriptionRequest;
import org.mika1212.subscription.entity.*;

import org.mika1212.subscription.repository.SubscriptionRepository;

import java.time.LocalDate;

@Service
public class SubscriptionService {

    private final SubscriptionRepository repository;

    public SubscriptionService(SubscriptionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ActivateSubscriptionResponse activate(ActivateSubscriptionRequest request) {

        validateActivationDate(request.activationDate());

        repository.findByUserIdAndStatus(request.userId(), SubscriptionStatus.ACTIVE)
                .ifPresent(s -> {
                    throw new SubscriptionAlreadyExistsException("User already has active subscription");
                });

        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setUserId(request.userId());
        entity.setType(request.type());
        entity.setStatus(SubscriptionStatus.ACTIVE);
        entity.setActivationDate(request.activationDate());

        repository.save(entity);
        return new ActivateSubscriptionResponse("OK", "Subscription activated");
    }

    @Transactional
    public DeactivateSubscriptionResponse deactivate(DeactivateSubscriptionRequest request) {

        SubscriptionEntity subscription = repository
                .findByUserIdAndStatus(request.userId(), SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new SubscriptionNotFoundException("No active subscription found"));

        subscription.setStatus(SubscriptionStatus.INACTIVE);
        subscription.setDeactivationDate(LocalDate.now());

        repository.save(subscription);
        return new DeactivateSubscriptionResponse("OK",  "Subscription deactivated");
    }

    private void validateActivationDate(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new SubscriptionActivateDateException("Activation date cannot be in the past");
        }
    }
}
