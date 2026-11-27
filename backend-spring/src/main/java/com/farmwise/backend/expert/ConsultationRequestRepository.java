package com.farmwise.backend.expert;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequestEntity, UUID> {
    List<ConsultationRequestEntity> findByFarmerIdOrderByCreatedAtDesc(UUID farmerId);

    List<ConsultationRequestEntity> findByExpertIdOrderByCreatedAtDesc(UUID expertId);
}
