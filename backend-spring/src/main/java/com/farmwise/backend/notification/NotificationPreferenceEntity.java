package com.farmwise.backend.notification;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreferenceEntity extends BaseAuditEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "in_app_enabled", nullable = false)
    private boolean inAppEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = false;

    @Column(name = "sms_enabled", nullable = false)
    private boolean smsEnabled = false;

    @Column(name = "cultivation_reminders", nullable = false)
    private boolean cultivationReminders = true;

    @Column(name = "weather_alerts", nullable = false)
    private boolean weatherAlerts = true;

    @Column(name = "inventory_alerts", nullable = false)
    private boolean inventoryAlerts = true;

    @Column(name = "order_updates", nullable = false)
    private boolean orderUpdates = true;

    @Column(name = "expert_updates", nullable = false)
    private boolean expertUpdates = true;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public boolean isInAppEnabled() {
        return inAppEnabled;
    }

    public void setInAppEnabled(boolean inAppEnabled) {
        this.inAppEnabled = inAppEnabled;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    public void setSmsEnabled(boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }

    public boolean isCultivationReminders() {
        return cultivationReminders;
    }

    public void setCultivationReminders(boolean cultivationReminders) {
        this.cultivationReminders = cultivationReminders;
    }

    public boolean isWeatherAlerts() {
        return weatherAlerts;
    }

    public void setWeatherAlerts(boolean weatherAlerts) {
        this.weatherAlerts = weatherAlerts;
    }

    public boolean isInventoryAlerts() {
        return inventoryAlerts;
    }

    public void setInventoryAlerts(boolean inventoryAlerts) {
        this.inventoryAlerts = inventoryAlerts;
    }

    public boolean isOrderUpdates() {
        return orderUpdates;
    }

    public void setOrderUpdates(boolean orderUpdates) {
        this.orderUpdates = orderUpdates;
    }

    public boolean isExpertUpdates() {
        return expertUpdates;
    }

    public void setExpertUpdates(boolean expertUpdates) {
        this.expertUpdates = expertUpdates;
    }
}
