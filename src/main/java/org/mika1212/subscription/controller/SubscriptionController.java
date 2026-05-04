package org.mika1212.subscription.controller;

import jakarta.validation.Valid;
import org.mika1212.subscription.dto.*;
import org.mika1212.subscription.service.SubscriptionService;
import org.modelmapper.ModelMapper;
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

        ActivateSubscriptionDTO dto = new ActivateSubscriptionDTO(
                request.userId(),
                request.type(),
                request.activationDate()
        );
        return subscriptionService.activate(dto);
    }

    @PostMapping("/deactivate")
    public DeactivateSubscriptionResponse deactivate(@RequestBody @Valid DeactivateSubscriptionRequest request) {

        DeactivateSubscriptionDTO dto = new DeactivateSubscriptionDTO(
                request.userId(),
                request.type()
        );
        return subscriptionService.deactivate(dto);
    }
}
