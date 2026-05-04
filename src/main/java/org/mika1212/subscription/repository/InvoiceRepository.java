package org.mika1212.subscription.repository;

import org.mika1212.common.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {
}
