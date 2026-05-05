package org.mika1212.subscription.e2e;

import org.junit.jupiter.api.Test;
import org.mika1212.common.entity.SubscriptionEntity;
import org.mika1212.common.entity.SubscriptionStatus;
import org.mika1212.subscription.entity.*;
import org.mika1212.subscription.repository.OutboxRepository;
import org.mika1212.subscription.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SubscriptionE2ETest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Test
    void shouldActivateSubscription_andCreateOutboxEvent() throws Exception {

        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/subscriptions/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "userId": "%s",
                            "type": "BASIC",
                            "activationDate": "%s"
                        }
                        """.formatted(userId, LocalDate.now().plusDays(1))
                        ))
                .andDo(print())
                .andExpect(status().isOk());

        // 1. subscription saved
        List<SubscriptionEntity> subs =
                subscriptionRepository.findAll();

        assertThat(subs).hasSize(1);

        SubscriptionEntity sub = subs.get(0);

        assertThat(sub.getUserId()).isEqualTo(userId);
        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);

        // 2. outbox event created
        List<OutboxEventEntity> events =
                outboxRepository.findAll();

        assertThat(events).hasSize(1);

        OutboxEventEntity event = events.get(0);

        assertThat(event.getEventType())
                .isEqualTo(OutboxEventType.SUBSCRIPTION_ACTIVATED);

        assertThat(event.getStatus())
                .isEqualTo(OutboxEventStatus.NEW);
    }
}
