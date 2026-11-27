package com.farmwise.backend.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Page<UserEntity> findByPrimaryRoleIgnoreCase(String primaryRole, Pageable pageable);

    long countByPrimaryRole(String primaryRole);

    long countByActiveTrue();
}
