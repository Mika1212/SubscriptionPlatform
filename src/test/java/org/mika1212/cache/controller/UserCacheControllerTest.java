package org.mika1212.cache.controller;

import org.junit.jupiter.api.Test;
import org.mika1212.cache.entity.InvoiceView;
import org.mika1212.cache.entity.SubscriptionView;
import org.mika1212.cache.entity.UserCacheDto;
import org.mika1212.cache.service.UserCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserCacheController.class)
class UserCacheControllerTest {

    @MockBean
    private UserCacheService service;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_return_user_cache_successfully() throws Exception {

        UUID userId = UUID.randomUUID();

        UserCacheDto cache = new UserCacheDto(
                List.of(
                        new SubscriptionView(
                                userId,
                                UUID.randomUUID(),
                                org.mika1212.common.entity.SubscriptionType.BASIC,
                                LocalDate.now(),
                                org.mika1212.common.entity.SubscriptionStatus.ACTIVE
                        )
                ),
                List.of(
                        new InvoiceView(
                                UUID.randomUUID(),
                                BigDecimal.valueOf(100),
                                LocalDate.now()
                        )
                )
        );

        when(service.getUserCache(userId)).thenReturn(cache);

        mockMvc.perform(get("/api/users/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptions").isArray())
                .andExpect(jsonPath("$.subscriptions.length()").value(1))
                .andExpect(jsonPath("$.invoices").isArray())
                .andExpect(jsonPath("$.invoices.length()").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalInvoices").value(1));
    }

    @Test
    void should_return_empty_cache_when_service_returns_empty() throws Exception {

        UUID userId = UUID.randomUUID();

        when(service.getUserCache(userId)).thenReturn(new UserCacheDto());

        mockMvc.perform(get("/api/users/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptions").isArray())
                .andExpect(jsonPath("$.invoices").isArray())
                .andExpect(jsonPath("$.totalInvoices").value(0));
    }

    @Test
    void should_fail_when_page_negative() throws Exception {

        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/users/{userId}", userId)
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_fail_when_size_too_large() throws Exception {

        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/users/{userId}", userId)
                        .param("page", "0")
                        .param("size", "999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_fail_when_size_zero() throws Exception {

        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/users/{userId}", userId)
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}
