package com.farmwise.backend.crop;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmCropRepository extends JpaRepository<FarmCropEntity, UUID> {
    Page<FarmCropEntity> findByFarmerIdOrderByCreatedAtDesc(UUID farmerId, Pageable pageable);

    Page<FarmCropEntity> findByFarmerIdAndFarmIdOrderByCreatedAtDesc(
            UUID farmerId, UUID farmId, Pageable pageable);

    List<FarmCropEntity> findByFarmerId(UUID farmerId);

    List<FarmCropEntity> findByFarmId(UUID farmId);

    long countByFarmerIdAndCultivationStatusIn(UUID farmerId, List<String> statuses);
}
