package com.farmwise.backend.marketplace;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategoryEntity, UUID> {
    List<ProductCategoryEntity> findByActiveTrueOrderByNameAsc();
}
