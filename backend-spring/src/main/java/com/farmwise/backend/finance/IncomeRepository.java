package com.farmwise.backend.finance;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeRepository extends JpaRepository<IncomeEntity, UUID> {
    Page<IncomeEntity> findByFarmerIdOrderByIncomeDateDescCreatedAtDesc(UUID farmerId, Pageable pageable);

    List<IncomeEntity> findByFarmerIdOrderByIncomeDateDesc(UUID farmerId);
}
