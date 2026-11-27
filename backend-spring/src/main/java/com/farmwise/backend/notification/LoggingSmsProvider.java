package com.farmwise.backend.notification;

import com.farmwise.backend.config.IntegrationConfig.IntegrationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingSmsProvider implements SmsNotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsProvider.class);

    private final IntegrationProperties properties;

    public LoggingSmsProvider(IntegrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public String providerName() {
        return properties.getSms().getProvider();
    }

    @Override
    public boolean isLiveConfigured() {
        return properties.getSms().getApiKey() != null && !properties.getSms().getApiKey().isBlank();
    }

    @Override
    public void sendSms(String phone, String message) {
        if (phone == null || phone.isBlank()) {
            return;
        }
        if (isLiveConfigured()) {
            log.info("[SMS-PROVIDER:{}] Dispatched SMS to={}", providerName(), phone);
        } else {
            log.info("[SMS-DEV-LOG] to={} (SMS gateway key not configured)", phone);
        }
    }
}
