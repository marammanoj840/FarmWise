package com.farmwise.backend.farm;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmLocationRepository extends JpaRepository<FarmLocationEntity, UUID> {
    Optional<FarmLocationEntity> findByFarmId(UUID farmId);
}
