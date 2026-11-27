package com.farmwise.backend.storage;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileObjectRepository extends JpaRepository<FileObjectEntity, UUID> {

    List<FileObjectEntity> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    List<FileObjectEntity> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    long countByOwnerId(UUID ownerId);
}
