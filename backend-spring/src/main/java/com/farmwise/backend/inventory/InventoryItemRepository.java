package com.farmwise.backend.inventory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemRepository extends JpaRepository<InventoryItemEntity, UUID> {
    Page<InventoryItemEntity> findByFarmerIdOrderByUpdatedAtDesc(UUID farmerId, Pageable pageable);

    Page<InventoryItemEntity> findByFarmerIdAndCategoryIgnoreCaseOrderByUpdatedAtDesc(
            UUID farmerId, String category, Pageable pageable);

    List<InventoryItemEntity> findByFarmerId(UUID farmerId);
}
