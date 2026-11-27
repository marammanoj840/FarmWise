package com.farmwise.backend.soil;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CropKnowledgeBaseRepository extends JpaRepository<CropKnowledgeBaseEntity, UUID> {
    List<CropKnowledgeBaseEntity> findByCropNameContainingIgnoreCaseOrSuitableSoilTypesContainingIgnoreCase(
            String cropName, String soilType);

    List<CropKnowledgeBaseEntity> findAllByOrderByCropNameAsc();
}
