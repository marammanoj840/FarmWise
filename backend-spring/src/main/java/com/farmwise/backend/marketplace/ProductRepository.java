package com.farmwise.backend.marketplace;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    Page<ProductEntity> findByApprovalStatusAndActiveTrue(String approvalStatus, Pageable pageable);

    Page<ProductEntity> findByCategoryIdAndApprovalStatusAndActiveTrue(
            UUID categoryId, String approvalStatus, Pageable pageable);

    Page<ProductEntity> findByNameContainingIgnoreCaseAndApprovalStatusAndActiveTrue(
            String name, String approvalStatus, Pageable pageable);

    List<ProductEntity> findBySupplierIdOrderByCreatedAtDesc(UUID supplierId);

    Page<ProductEntity> findByApprovalStatusOrderByCreatedAtDesc(String approvalStatus, Pageable pageable);

    long countByApprovalStatus(String approvalStatus);
}
