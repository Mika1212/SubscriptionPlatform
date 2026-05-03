package org.mika1212.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "outbox_events")
public class OutboxEventEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private OutboxEventType eventType;

    @Column(columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxEventStatus status;

    private Instant createdAt;
}
