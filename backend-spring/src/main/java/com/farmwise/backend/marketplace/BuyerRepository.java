package com.farmwise.backend.marketplace;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerRepository extends JpaRepository<BuyerEntity, UUID> {
    Optional<BuyerEntity> findByUserId(UUID userId);
}
