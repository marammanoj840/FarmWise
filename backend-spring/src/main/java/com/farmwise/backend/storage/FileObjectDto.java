package com.farmwise.backend.storage;

import java.util.UUID;

/**
 * DTO returned when querying file metadata.
 */
public record FileObjectDto(
        UUID id,
        UUID ownerId,
        String entityType,
        UUID entityId,
        String originalFilename,
        String contentType,
        long fileSizeBytes,
        String publicUrl,
        String createdAt) {

    public static FileObjectDto from(FileObjectEntity entity) {
        return new FileObjectDto(
                entity.getId(),
                entity.getOwnerId(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getOriginalFilename(),
                entity.getContentType(),
                entity.getFileSizeBytes(),
                entity.getPublicUrl(),
                entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null);
    }
}
