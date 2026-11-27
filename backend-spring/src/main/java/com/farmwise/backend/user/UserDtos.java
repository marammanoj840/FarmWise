package com.farmwise.backend.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class UserDtos {

    private UserDtos() {
    }

    public record FullUserProfileResponse(
            UUID userId,
            String email,
            String fullName,
            String phone,
            String primaryRole,
            List<String> roles,
            boolean active,
            boolean emailVerified,
            OffsetDateTime lastLoginAt,
            String avatarUrl,
            String bio,
            String addressLine,
            String villageOrCity,
            String district,
            String state,
            String country,
            String postalCode,
            String preferredLanguage,
            String preferredCurrency,
            Integer farmExperienceYears) {
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(min = 2, max = 120) String fullName,
            @Size(max = 32) String phone,
            @Size(max = 500) String avatarUrl,
            @Size(max = 2000) String bio,
            @Size(max = 255) String addressLine,
            @Size(max = 120) String villageOrCity,
            @Size(max = 120) String district,
            @Size(max = 120) String state,
            @Size(max = 100) String country,
            @Size(max = 20) String postalCode,
            @Size(max = 16) String preferredLanguage,
            @Size(max = 8) String preferredCurrency,
            @Min(0) @Max(80) Integer farmExperienceYears) {
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, max = 100) String newPassword) {
    }

    public record SessionItem(
            UUID id,
            String userAgent,
            String ipAddress,
            OffsetDateTime createdAt,
            OffsetDateTime expiresAt) {
    }
}
