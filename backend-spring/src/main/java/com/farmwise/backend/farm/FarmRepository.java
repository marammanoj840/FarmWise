package com.farmwise.backend.farm;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmRepository extends JpaRepository<FarmEntity, UUID> {
    Page<FarmEntity> findByFarmerIdOrderByCreatedAtDesc(UUID farmerId, Pageable pageable);

    Page<FarmEntity> findByFarmerIdAndNameContainingIgnoreCaseOrderByCreatedAtDesc(
            UUID farmerId, String name, Pageable pageable);

    List<FarmEntity> findByFarmerId(UUID farmerId);

    long countByFarmerId(UUID farmerId);
}
