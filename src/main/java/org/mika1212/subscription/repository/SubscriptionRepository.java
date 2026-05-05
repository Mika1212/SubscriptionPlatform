package org.mika1212.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.mika1212.common.entity.SubscriptionEntity;
import org.mika1212.common.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, UUID> {

    Optional<SubscriptionEntity> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);

    @Query(value = """
    SELECT * FROM subscriptions
    WHERE status = 'ACTIVE'
    AND next_billing_date <= :today
    ORDER BY next_billing_date, id
    LIMIT :limit
    """, nativeQuery = true)
    List<SubscriptionEntity> claimBatch(LocalDate today, int limit);
}
