package com.farmwise.backend.storage;

import com.farmwise.backend.config.IntegrationConfig.IntegrationProperties;
import com.farmwise.backend.exception.BusinessValidationException;
import com.farmwise.backend.exception.ResourceNotFoundException;
import com.farmwise.backend.security.SecurityUtils;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    /** Allowed entity types for file uploads */
    private static final Set<String> ALLOWED_ENTITY_TYPES = Set.of(
            "FARM_PHOTO", "CROP_PHOTO", "PRODUCT_IMAGE", "USER_AVATAR", "DOCUMENT");

    /** Allowed MIME types */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    /** Allowed file extensions (lowercase) */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif", ".pdf", ".doc", ".docx");

    private final FileObjectRepository repository;
    private final FileStorageProvider storageProvider;
    private final IntegrationProperties.Storage storageConfig;

    public StorageService(FileObjectRepository repository,
                          FileStorageProvider storageProvider,
                          IntegrationProperties integrationProperties) {
        this.repository = repository;
        this.storageProvider = storageProvider;
        this.storageConfig = integrationProperties.getStorage();
    }

    public FileObjectDto upload(MultipartFile file, String entityType, UUID entityId) {
        UUID userId = SecurityUtils.requireCurrentUserId();

        // Validate entity type
        if (!ALLOWED_ENTITY_TYPES.contains(entityType)) {
            throw new BusinessValidationException("Invalid entity type: " + entityType
                    + ". Allowed: " + ALLOWED_ENTITY_TYPES);
        }

        // Validate file is not empty
        if (file.isEmpty()) {
            throw new BusinessValidationException("File is empty");
        }

        // Validate file size
        if (file.getSize() > storageConfig.getMaxFileSizeBytes()) {
            throw new BusinessValidationException("File size exceeds maximum allowed: "
                    + (storageConfig.getMaxFileSizeBytes() / (1024 * 1024)) + " MB");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessValidationException("File type not allowed: " + contentType
                    + ". Allowed: " + ALLOWED_CONTENT_TYPES);
        }

        // Validate extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String ext = getExtension(originalFilename);
            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                throw new BusinessValidationException("File extension not allowed: " + ext);
            }
        }

        // Generate storage key
        String storageKey = entityType.toLowerCase() + "/"
                + userId + "/"
                + UUID.randomUUID() + getExtension(originalFilename != null ? originalFilename : "file.bin");

        // Upload to provider
        String publicUrl = null;
        String provider = storageConfig.getProvider();
        if (storageProvider.isConfigured()) {
            publicUrl = storageProvider.upload(file, storageKey);
            provider = "s3";
        } else {
            log.info("S3 not configured; saving metadata only for key={}", storageKey);
            provider = "local-metadata";
        }

        // Persist metadata
        FileObjectEntity entity = new FileObjectEntity();
        entity.setOwnerId(userId);
        entity.setEntityType(entityType);
        entity.setEntityId(entityId);
        entity.setOriginalFilename(originalFilename != null ? originalFilename : "unknown");
        entity.setStorageKey(storageKey);
        entity.setContentType(contentType);
        entity.setFileSizeBytes(file.getSize());
        entity.setStorageProvider(provider);
        entity.setPublicUrl(publicUrl);

        FileObjectEntity saved = repository.save(entity);
        log.info("File uploaded: id={}, key={}, size={}", saved.getId(), storageKey, file.getSize());

        return FileObjectDto.from(saved);
    }

    public List<FileObjectDto> listMyFiles() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        return repository.findByOwnerIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(FileObjectDto::from)
                .toList();
    }

    public FileObjectDto getFile(UUID fileId) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        FileObjectEntity entity = repository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + fileId));
        // Ownership check: owner or admin
        SecurityUtils.assertOwnerOrAdmin(entity.getOwnerId(), "file");
        return FileObjectDto.from(entity);
    }

    public void deleteFile(UUID fileId) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        FileObjectEntity entity = repository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + fileId));
        SecurityUtils.assertOwnerOrAdmin(entity.getOwnerId(), "file");

        // Delete from storage if S3 configured
        if (storageProvider.isConfigured()) {
            try {
                storageProvider.delete(entity.getStorageKey());
            } catch (Exception e) {
                log.warn("Failed to delete from S3: key={}", entity.getStorageKey(), e);
            }
        }

        repository.delete(entity);
        log.info("File deleted: id={}, key={}", fileId, entity.getStorageKey());
    }

    public boolean isStorageConfigured() {
        return storageProvider.isConfigured();
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot).toLowerCase() : "";
    }
}
