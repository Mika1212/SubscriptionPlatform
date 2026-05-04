package org.mika1212.subscription.controller;

import org.junit.jupiter.api.Test;
import org.mika1212.subscription.dto.*;
import org.mika1212.common.entity.SubscriptionType;
import org.mika1212.subscription.service.SubscriptionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {
    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private ModelMapper modelMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void activate_success() throws Exception {

        UUID userId = UUID.randomUUID();

        ActivateSubscriptionRequest request = new ActivateSubscriptionRequest(
                userId,
                SubscriptionType.BASIC,
                LocalDate.now().plusDays(1)
        );

        ActivateSubscriptionDTO dto = new ActivateSubscriptionDTO(
                userId,
                SubscriptionType.BASIC,
                request.activationDate()
        );

        when(modelMapper.map(any(), eq(ActivateSubscriptionDTO.class)))
                .thenReturn(dto);

        when(subscriptionService.activate(dto))
                .thenReturn(new ActivateSubscriptionResponse("OK", "Subscription activated"));

        mockMvc.perform(post("/api/subscriptions/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "userId": "%s",
                                "type": "BASIC",
                                "activationDate": "%s"
                            }
                            """.formatted(userId, request.activationDate())
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Subscription activated"));
    }

    @Test
    void deactivate_success() throws Exception {

        UUID userId = UUID.randomUUID();

        DeactivateSubscriptionDTO dto =
                new DeactivateSubscriptionDTO(userId, SubscriptionType.BASIC);

        when(modelMapper.map(any(), eq(DeactivateSubscriptionDTO.class)))
                .thenReturn(dto);

        when(subscriptionService.deactivate(dto))
                .thenReturn(new DeactivateSubscriptionResponse("OK", "Subscription deactivated"));

        mockMvc.perform(post("/api/subscriptions/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "userId": "%s",
                                "type": "BASIC"
                            }
                            """.formatted(userId)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Subscription deactivated"));
    }

    @Test
    void activate_shouldReturnBadRequest_whenInvalidPayload() throws Exception {

        mockMvc.perform(post("/api/subscriptions/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "userId": "not-a-uuid",
                                "type": "SILVER",
                                "activationDate": "invalid-date"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void activate_shouldReturnBadRequest_whenMissingFields() throws Exception {

        mockMvc.perform(post("/api/subscriptions/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "userId": null,
                                "type": null,
                                "activationDate": null
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void activate_shouldReturnBadRequest_whenOnlyDateInvalid() throws Exception {

        mockMvc.perform(post("/api/subscriptions/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "userId": "%s",
                                "type": "BASIC",
                                "activationDate": "2020-13-99"
                            }
                            """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }
}
