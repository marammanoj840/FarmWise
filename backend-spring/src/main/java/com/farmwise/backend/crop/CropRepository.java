package com.farmwise.backend.crop;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CropRepository extends JpaRepository<CropEntity, UUID> {
    List<CropEntity> findAllByOrderByNameAsc();
}
