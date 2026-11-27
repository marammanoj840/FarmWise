package com.farmwise.backend.finance;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, UUID> {
    Page<ExpenseEntity> findByFarmerIdOrderByExpenseDateDescCreatedAtDesc(UUID farmerId, Pageable pageable);

    List<ExpenseEntity> findByFarmerIdOrderByExpenseDateDesc(UUID farmerId);

    List<ExpenseEntity> findTop5ByFarmerIdOrderByExpenseDateDescCreatedAtDesc(UUID farmerId);
}
