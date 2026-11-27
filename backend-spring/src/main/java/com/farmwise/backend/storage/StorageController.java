package com.farmwise.backend.storage;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/storage")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Upload a file. Requires authentication.
     * entityType: FARM_PHOTO, CROP_PHOTO, PRODUCT_IMAGE, USER_AVATAR, DOCUMENT
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileObjectDto> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType,
            @RequestParam(value = "entityId", required = false) UUID entityId) {
        FileObjectDto dto = storageService.upload(file, entityType, entityId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * List all files owned by the current user.
     */
    @GetMapping("/my-files")
    public ResponseEntity<List<FileObjectDto>> listMyFiles() {
        return ResponseEntity.ok(storageService.listMyFiles());
    }

    /**
     * Get file metadata by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FileObjectDto> getFile(@PathVariable UUID id) {
        return ResponseEntity.ok(storageService.getFile(id));
    }

    /**
     * Delete a file by ID. Only the owner or an admin can delete.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
        storageService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check storage configuration status.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "configured", storageService.isStorageConfigured(),
                "message", storageService.isStorageConfigured()
                        ? "S3-compatible storage is configured and ready"
                        : "S3 storage not configured; uploads will save metadata only"));
    }
}
