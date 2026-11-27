package com.farmwise.backend.storage;

import com.farmwise.backend.config.IntegrationConfig.IntegrationProperties;
import com.farmwise.backend.exception.BusinessValidationException;
import com.farmwise.backend.security.FarmWiseUserPrincipal;
import com.farmwise.backend.user.UserEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StorageServiceTest {

    private FileObjectRepository repository;
    private FileStorageProvider storageProvider;
    private IntegrationProperties integrationProperties;
    private StorageService storageService;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        repository = mock(FileObjectRepository.class);
        storageProvider = mock(FileStorageProvider.class);
        integrationProperties = new IntegrationProperties();
        integrationProperties.getStorage().setMaxFileSizeBytes(5 * 1024 * 1024); // 5 MB

        storageService = new StorageService(repository, storageProvider, integrationProperties);

        testUserId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(testUserId);
        user.setEmail("farmer@farmwise.ag");
        user.setFullName("Farmer Joe");
        user.setPrimaryRole("FARMER");
        user.setActive(true);

        FarmWiseUserPrincipal principal = new FarmWiseUserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsInvalidEntityType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        assertThrows(BusinessValidationException.class, () ->
                storageService.upload(file, "INVALID_ENTITY_TYPE", null));
    }

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThrows(BusinessValidationException.class, () ->
                storageService.upload(emptyFile, "FARM_PHOTO", null));
    }

    @Test
    void rejectsExcessiveFileSize() {
        byte[] largeBytes = new byte[6 * 1024 * 1024]; // 6 MB > 5 MB limit
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large.png", "image/png", largeBytes);

        assertThrows(BusinessValidationException.class, () ->
                storageService.upload(largeFile, "FARM_PHOTO", null));
    }

    @Test
    void rejectsDisallowedMimeType() {
        MockMultipartFile exeFile = new MockMultipartFile(
                "file", "malware.exe", "application/x-msdownload", new byte[]{1, 2, 3});

        assertThrows(BusinessValidationException.class, () ->
                storageService.upload(exeFile, "DOCUMENT", null));
    }

    @Test
    void rejectsDisallowedExtension() {
        MockMultipartFile badExtFile = new MockMultipartFile(
                "file", "script.sh", "image/jpeg", new byte[]{1, 2, 3});

        assertThrows(BusinessValidationException.class, () ->
                storageService.upload(badExtFile, "FARM_PHOTO", null));
    }

    @Test
    void successfulUploadWithMetadataOnlyWhenS3NotConfigured() {
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "farm_overview.jpg", "image/jpeg", new byte[]{1, 2, 3, 4, 5});

        when(storageProvider.isConfigured()).thenReturn(false);
        when(repository.save(any(FileObjectEntity.class))).thenAnswer(invocation -> {
            FileObjectEntity e = invocation.getArgument(0);
            return e;
        });

        FileObjectDto dto = storageService.upload(validFile, "FARM_PHOTO", UUID.randomUUID());

        assertNotNull(dto);
        assertEquals("FARM_PHOTO", dto.entityType());
        assertEquals("farm_overview.jpg", dto.originalFilename());
        assertEquals("image/jpeg", dto.contentType());
        assertEquals(5L, dto.fileSizeBytes());
        verify(storageProvider, never()).upload(any(), any());
    }

    @Test
    void successfulUploadWithS3ProviderWhenConfigured() {
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "soil_report.pdf", "application/pdf", new byte[]{10, 20, 30});

        when(storageProvider.isConfigured()).thenReturn(true);
        when(storageProvider.upload(any(), any())).thenReturn("https://s3.example.com/soil_report.pdf");
        when(repository.save(any(FileObjectEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FileObjectDto dto = storageService.upload(validFile, "DOCUMENT", null);

        assertNotNull(dto);
        assertEquals("https://s3.example.com/soil_report.pdf", dto.publicUrl());
        verify(storageProvider, times(1)).upload(any(), any());
    }
}
