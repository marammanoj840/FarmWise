package com.farmwise.backend.expert;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    List<AppointmentEntity> findByFarmerIdOrderByScheduledStartAsc(UUID farmerId);

    List<AppointmentEntity> findByExpertIdOrderByScheduledStartAsc(UUID expertId);
}
