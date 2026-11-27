package com.farmwise.backend.crop;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CropVarietyRepository extends JpaRepository<CropVarietyEntity, UUID> {
    List<CropVarietyEntity> findByCropIdOrderByNameAsc(UUID cropId);
}
