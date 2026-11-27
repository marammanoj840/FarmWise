package com.farmwise.backend.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record PlatformStatsResponse(
            long totalUsers,
            long activeUsers,
            Map<String, Long> usersByRole,
            long totalFarms,
            long totalActiveCrops,
            long totalProducts,
            long pendingProducts,
            long activeProduceListings,
            long totalOrders,
            long totalExperts,
            long totalSuppliers,
            long openContactInquiries) {
    }

    public record UserStatusUpdateRequest(boolean active) {
    }

    public record UserRoleUpdateRequest(@NotBlank String role) {
    }

    public record ModerationDecisionRequest(
            @NotBlank String status,
            @Size(max = 500) String note) {
    }

    public record BroadcastNotificationRequest(
            UUID targetUserId,
            @NotBlank @Size(max = 40) String notificationType,
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 2000) String message,
            @Size(max = 255) String actionUrl) {
    }

    public record UpdateSettingRequest(
            @NotBlank @Size(max = 2000) String value) {
    }

    public record ContactInquiryUpdateRequest(
            @NotBlank String status,
            @Size(max = 2000) String adminNotes) {
    }

    public record AuditLogItem(
            UUID id,
            UUID actorUserId,
            String actorEmail,
            String action,
            String entityType,
            String entityId,
            String detailsJson,
            String ipAddress,
            OffsetDateTime createdAt) {
    }

    public record IntegrationHealthItem(
            String integration,
            String provider,
            boolean configured,
            String statusNote) {
    }
}
