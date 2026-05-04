package org.mika1212.cache.service;

import lombok.extern.slf4j.Slf4j;
import org.mika1212.cache.entity.InvoiceView;
import org.mika1212.cache.entity.SubscriptionView;
import org.mika1212.cache.entity.UserCacheDto;
import org.mika1212.common.entity.SubscriptionStatus;
import org.mika1212.common.entity.UserSubscriptionDeactivatedEvent;
import org.mika1212.common.json.JacksonJsonSerializer;
import org.mika1212.common.entity.InvoiceCreatedEvent;
import org.mika1212.common.entity.UserSubscriptionActivatedEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class UserCacheService {

    private final StringRedisTemplate redisTemplate;
    private final JacksonJsonSerializer serializer;

    public UserCacheService(StringRedisTemplate redisTemplate,
                            JacksonJsonSerializer serializer) {
        this.redisTemplate = redisTemplate;
        this.serializer = serializer;
    }

    public void applySubscriptionEvent(UserSubscriptionActivatedEvent event) {

        String key = key(event.userId());

        UserCacheDto cache = getOrCreate(key);

        cache.getSubscriptions().removeIf(s ->
                s.subscriptionId().equals(event.subscriptionId())
        );

        cache.getSubscriptions().add(
                new SubscriptionView(
                        event.userId(),
                        event.subscriptionId(),
                        event.type(),
                        event.activationDate(),
                        SubscriptionStatus.ACTIVE
                )
        );

        save(key, cache);
    }

    public void applySubscriptionEvent(UserSubscriptionDeactivatedEvent event) {

        String key = key(event.userId());

        UserCacheDto cache = getOrCreate(key);

        cache.getSubscriptions().forEach(sub -> {
            if (sub.type() == event.type()) {
                cache.getSubscriptions().remove(sub);
            }
        });

        save(key, cache);
    }

    public void applyInvoiceEvent(InvoiceCreatedEvent event) {

        String key = key(event.userId());

        UserCacheDto cache = getOrCreate(key);

        cache.getInvoices().removeIf(i ->
                i.invoiceId().equals(event.invoiceId())
        );

        cache.getInvoices().add(
                new InvoiceView(
                        event.invoiceId(),
                        event.amount(),
                        event.billingDate()
                )
        );

        save(key, cache);
    }

    private String key(UUID userId) {
        return "user:" + userId;
    }

    private UserCacheDto getOrCreate(String key) {

        try {
            String raw = redisTemplate.opsForValue().get(key);

            if (raw == null) {
                return new UserCacheDto();
            }

            return serializer.toObject(raw, UserCacheDto.class);

        } catch (Exception e) {
            log.warn("Redis unavailable, returning empty cache, key={}", key, e);
            return new UserCacheDto();
        }
    }

    @Retryable(
            value = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )
    private void save(String key, UserCacheDto cache) {
        redisTemplate.opsForValue().set(key, serializer.toJson(cache));
    }
}