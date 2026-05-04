package org.mika1212.cache.consumer;

import lombok.extern.slf4j.Slf4j;
import org.mika1212.cache.service.UserCacheService;
import org.mika1212.common.entity.UserSubscriptionActivatedEvent;
import org.mika1212.common.entity.UserSubscriptionDeactivatedEvent;
import org.mika1212.common.json.JacksonJsonSerializer;
import org.mika1212.config.RabbitConfig;
import org.mika1212.common.entity.InvoiceCreatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserEventConsumer {

    private final UserCacheService cacheService;
    private final JacksonJsonSerializer serializer;

    public UserEventConsumer(UserCacheService cacheService,
                             JacksonJsonSerializer serializer) {
        this.cacheService = cacheService;
        this.serializer = serializer;
    }

    @RabbitListener(queues = RabbitConfig.SUBSCRIPTION_QUEUE)
    public void handleSubscriptionActivated(String message) {
        try {
            UserSubscriptionActivatedEvent event =
                    serializer.toObject(message, UserSubscriptionActivatedEvent.class);

            cacheService.applySubscriptionEvent(event);

        } catch (Exception e) {
            log.error("Failed to process subscription activated event", e);
        }
    }

    @RabbitListener(queues = RabbitConfig.UNSUBSCRIPTION_QUEUE)
    public void handleSubscriptionDeactivated(String message) {
        try {
            UserSubscriptionDeactivatedEvent event =
                    serializer.toObject(message, UserSubscriptionDeactivatedEvent.class);

            cacheService.applySubscriptionEvent(event);

        } catch (Exception e) {
            log.error("Failed to process subscription deactivated event", e);
        }
    }

    @RabbitListener(queues = RabbitConfig.INVOICE_QUEUE)
    public void handleInvoiceEvents(String message) {
        try {
            InvoiceCreatedEvent event =
                    serializer.toObject(message, InvoiceCreatedEvent.class);

            cacheService.applyInvoiceEvent(event);

        } catch (Exception e) {
            log.error("Failed to process invoice event", e);
        }
    }
}
