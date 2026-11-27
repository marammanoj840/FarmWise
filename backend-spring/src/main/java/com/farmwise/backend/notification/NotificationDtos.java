package com.farmwise.backend.notification;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class NotificationDtos {

    private NotificationDtos() {
    }

    public record NotificationResponse(
            UUID id,
            String notificationType,
            String title,
            String message,
            boolean read,
            OffsetDateTime readAt,
            String actionUrl,
            OffsetDateTime createdAt) {
    }

    public record NotificationPreferenceResponse(
            boolean inAppEnabled,
            boolean emailEnabled,
            boolean smsEnabled,
            boolean cultivationReminders,
            boolean weatherAlerts,
            boolean inventoryAlerts,
            boolean orderUpdates,
            boolean expertUpdates,
            boolean emailProviderConfigured,
            boolean smsProviderConfigured) {
    }

    public record UpdatePreferencesRequest(
            boolean inAppEnabled,
            boolean emailEnabled,
            boolean smsEnabled,
            boolean cultivationReminders,
            boolean weatherAlerts,
            boolean inventoryAlerts,
            boolean orderUpdates,
            boolean expertUpdates) {
    }

    public record UnreadCountResponse(long unreadCount) {
    }
}
