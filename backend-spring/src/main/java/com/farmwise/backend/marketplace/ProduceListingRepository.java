package com.farmwise.backend.marketplace;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProduceListingRepository extends JpaRepository<ProduceListingEntity, UUID> {
    Page<ProduceListingEntity> findByListingStatusAndModerationStatusOrderByCreatedAtDesc(
            String listingStatus, String moderationStatus, Pageable pageable);

    Page<ProduceListingEntity> findByCropNameContainingIgnoreCaseAndListingStatusAndModerationStatusOrderByCreatedAtDesc(
            String cropName, String listingStatus, String moderationStatus, Pageable pageable);

    List<ProduceListingEntity> findByFarmerIdOrderByCreatedAtDesc(UUID farmerId);

    Page<ProduceListingEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByListingStatus(String listingStatus);
}
