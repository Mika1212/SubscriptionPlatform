package org.mika1212.cache.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mika1212.cache.entity.SubscriptionView;
import org.mika1212.cache.entity.UserCacheDto;
import org.mika1212.common.entity.*;
import org.mika1212.common.json.JacksonJsonSerializer;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @Mock
    private JacksonJsonSerializer serializer;

    @InjectMocks
    private UserCacheService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void should_add_subscription() {
        UUID userId = UUID.randomUUID();
        UUID subId = UUID.randomUUID();

        when(redisTemplate.opsForValue().get(any())).thenReturn(null);

        service.applySubscriptionEvent(
                new UserSubscriptionActivatedEvent(
                        userId,
                        subId,
                        SubscriptionType.BASIC,
                        LocalDate.now()
                )
        );

        verify(redisTemplate.opsForValue(), atLeastOnce()).set(any(), any(), any(Duration.class));
    }

    @Test
    void should_remove_subscription_by_id_on_deactivation() {
        UUID userId = UUID.randomUUID();
        UUID subId = UUID.randomUUID();

        UserCacheDto cache = new UserCacheDto(
                new ArrayList<>(List.of(
                        new SubscriptionView(userId, subId, SubscriptionType.BASIC, LocalDate.now(), SubscriptionStatus.ACTIVE)
                )),
                new ArrayList<>()
        );

        when(valueOps.get(anyString())).thenReturn("json");
        when(serializer.toObject(any(), eq(UserCacheDto.class))).thenReturn(cache);

        service.applySubscriptionEvent(
                new UserSubscriptionDeactivatedEvent(userId, subId, SubscriptionType.BASIC)
        );

        verify(redisTemplate.opsForValue(), atLeastOnce()).set(any(), any(), any(Duration.class));
    }

    @Test
    void should_sort_invoices_desc() {
        UUID userId = UUID.randomUUID();

        UserCacheDto cache = new UserCacheDto(new ArrayList<>(), new ArrayList<>());

        when(valueOps.get(anyString())).thenReturn("json");
        when(serializer.toObject(any(), eq(UserCacheDto.class))).thenReturn(cache);

        service.applyInvoiceEvent(
                new InvoiceCreatedEvent(UUID.randomUUID(), userId, BigDecimal.TEN, LocalDate.now())
        );

        verify(redisTemplate.opsForValue(), atLeastOnce()).set(any(), any(), any(Duration.class));
    }
}
