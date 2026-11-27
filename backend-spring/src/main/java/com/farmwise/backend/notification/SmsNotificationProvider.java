package com.farmwise.backend.notification;

public interface SmsNotificationProvider {
    String providerName();

    boolean isLiveConfigured();

    void sendSms(String phone, String message);
}
