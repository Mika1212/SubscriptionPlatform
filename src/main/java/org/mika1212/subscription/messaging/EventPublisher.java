package org.mika1212.subscription.messaging;

import org.mika1212.subscription.entity.OutboxEventType;

public interface EventPublisher {
    void publish(OutboxEventType type, String message);
}
