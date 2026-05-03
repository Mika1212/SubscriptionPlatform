package org.mika1212.subscription.controller;

import jakarta.validation.Valid;
import org.mika1212.subscription.dto.ActivateSubscriptionRequest;
import org.mika1212.subscription.dto.ActivateSubscriptionResponse;
import org.mika1212.subscription.dto.DeactivateSubscriptionRequest;
import org.mika1212.subscription.dto.DeactivateSubscriptionResponse;
import org.mika1212.subscription.service.SubscriptionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/activate")
    public ActivateSubscriptionResponse activate(@RequestBody @Valid ActivateSubscriptionRequest request) {
        return subscriptionService.activate(request);
    }

    @PostMapping("/deactivate")
    public DeactivateSubscriptionResponse deactivate(@RequestBody @Valid DeactivateSubscriptionRequest request) {
        return subscriptionService.deactivate(request);
    }
}
