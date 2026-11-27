package com.farmwise.backend.soil;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CropObservationRepository extends JpaRepository<CropObservationEntity, UUID> {
    Page<CropObservationEntity> findByFarmerIdOrderByObservedAtDesc(UUID farmerId, Pageable pageable);
}
