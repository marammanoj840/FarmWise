package com.farmwise.backend.inventory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransactionEntity, UUID> {
    List<InventoryTransactionEntity> findByInventoryItemIdOrderByTransactionDateDescCreatedAtDesc(
            UUID inventoryItemId);
}
