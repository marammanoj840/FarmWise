package com.farmwise.backend.soil;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoilRecordRepository extends JpaRepository<SoilRecordEntity, UUID> {
    Page<SoilRecordEntity> findByFarmerIdOrderByTestDateDesc(UUID farmerId, Pageable pageable);

    List<SoilRecordEntity> findByFarmIdOrderByTestDateDesc(UUID farmId);
}
