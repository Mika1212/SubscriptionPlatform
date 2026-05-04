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
    private final ModelMapper modelMapper;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
        this.modelMapper = new ModelMapper();
    }

    @PostMapping("/activate")
    public ActivateSubscriptionResponse activate(@RequestBody @Valid ActivateSubscriptionRequest request) {
        ActivateSubscriptionDTO activateSubscriptionDto = modelMapper.map(request, ActivateSubscriptionDTO.class);
        return subscriptionService.activate(activateSubscriptionDto);
    }

    @PostMapping("/deactivate")
    public DeactivateSubscriptionResponse deactivate(@RequestBody @Valid DeactivateSubscriptionRequest request) {
        DeactivateSubscriptionDTO deactivateSubscriptionDto = modelMapper.map(request, DeactivateSubscriptionDTO.class);
        return subscriptionService.deactivate(deactivateSubscriptionDto);
    }
}
