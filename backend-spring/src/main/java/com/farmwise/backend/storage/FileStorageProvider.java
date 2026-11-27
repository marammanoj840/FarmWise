package com.farmwise.backend.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Pluggable file storage provider interface.
 * Implementations can target S3-compatible storage, local disk, or mock/metadata-only.
 */
public interface FileStorageProvider {

    /**
     * Upload a file and return the public/signed URL for access.
     *
     * @param file       the multipart file
     * @param storageKey the key/path to store the file under
     * @return the accessible URL for the stored file
     */
    String upload(MultipartFile file, String storageKey);

    /**
     * Delete a file by its storage key.
     */
    void delete(String storageKey);

    /**
     * Check if this provider is properly configured and usable.
     */
    boolean isConfigured();
}
