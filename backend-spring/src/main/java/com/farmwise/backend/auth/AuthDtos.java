package com.farmwise.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "Full name is required")
            @Size(min = 2, max = 120)
            String fullName,
            @NotBlank(message = "Email is required")
            @Email(message = "Valid email address is required")
            String email,
            @NotBlank(message = "Password is required")
            @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
            String password,
            @Size(max = 32)
            String phone,
            @NotBlank(message = "Role is required")
            String role,
            String organizationOrBusinessName,
            String city,
            String state,
            String specializations) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record LogoutRequest(String refreshToken) {
    }

    public record ForgotPasswordRequest(@NotBlank @Email String email) {
    }

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 8, max = 100) String newPassword) {
    }

    public record VerifyEmailRequest(@NotBlank String token) {
    }

    public record UserSummary(
            UUID id,
            String email,
            String fullName,
            String phone,
            String primaryRole,
            List<String> roles,
            boolean active,
            boolean emailVerified,
            OffsetDateTime lastLoginAt) {
    }

    public record AuthResponse(
            String tokenType,
            String accessToken,
            long expiresInSeconds,
            String refreshToken,
            UserSummary user,
            String devEmailVerificationToken) {
    }

    public record ForgotPasswordResponse(
            String message,
            String devResetTokenPreview) {
    }
}
