package com.farmwise.backend.expert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertRepository extends JpaRepository<ExpertEntity, UUID> {
    Optional<ExpertEntity> findByUserId(UUID userId);

    Page<ExpertEntity> findByVerificationStatusIgnoreCase(String status, Pageable pageable);

    List<ExpertEntity> findBySpecializationsContainingIgnoreCaseOrHeadlineContainingIgnoreCase(
            String specialization, String headline);
}
