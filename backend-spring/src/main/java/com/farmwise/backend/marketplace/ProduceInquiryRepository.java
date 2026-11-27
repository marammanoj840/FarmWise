package com.farmwise.backend.marketplace;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProduceInquiryRepository extends JpaRepository<ProduceInquiryEntity, UUID> {
    List<ProduceInquiryEntity> findByProduceListingIdOrderByCreatedAtDesc(UUID produceListingId);

    List<ProduceInquiryEntity> findByBuyerUserIdOrderByCreatedAtDesc(UUID buyerUserId);
}
