package com.farmwise.backend.admin;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactInquiryRepository extends JpaRepository<ContactInquiryEntity, UUID> {
    Page<ContactInquiryEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
