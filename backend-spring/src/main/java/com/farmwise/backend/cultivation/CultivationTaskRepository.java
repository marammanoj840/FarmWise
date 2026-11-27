package com.farmwise.backend.cultivation;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CultivationTaskRepository extends JpaRepository<CultivationTaskEntity, UUID> {
    Page<CultivationTaskEntity> findByFarmerIdOrderByDueDateAsc(UUID farmerId, Pageable pageable);

    Page<CultivationTaskEntity> findByFarmerIdAndStatusOrderByDueDateAsc(
            UUID farmerId, String status, Pageable pageable);

    List<CultivationTaskEntity> findTop5ByFarmerIdAndStatusInOrderByDueDateAsc(
            UUID farmerId, List<String> statuses);

    long countByFarmerIdAndStatusIn(UUID farmerId, List<String> statuses);
}
