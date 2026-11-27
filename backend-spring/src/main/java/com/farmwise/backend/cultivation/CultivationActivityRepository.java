package com.farmwise.backend.cultivation;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CultivationActivityRepository extends JpaRepository<CultivationActivityEntity, UUID> {
    Page<CultivationActivityEntity> findByFarmerIdOrderByActivityDateDesc(UUID farmerId, Pageable pageable);

    Page<CultivationActivityEntity> findByFarmerIdAndFarmIdOrderByActivityDateDesc(
            UUID farmerId, UUID farmId, Pageable pageable);

    List<CultivationActivityEntity> findByFarmerIdOrderByActivityDateDesc(UUID farmerId);
}
