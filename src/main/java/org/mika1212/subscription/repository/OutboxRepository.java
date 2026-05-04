package org.mika1212.subscription.repository;

import org.mika1212.subscription.entity.OutboxEventEntity;
import org.mika1212.subscription.entity.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEventEntity, UUID> {

    List<OutboxEventEntity> findTop50ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);
}
