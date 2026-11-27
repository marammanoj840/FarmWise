package com.farmwise.backend.notification;

public interface EmailNotificationProvider {
    String providerName();

    boolean isLiveConfigured();

    void sendEmail(String toEmail, String subject, String body);
}
