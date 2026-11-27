package com.farmwise.backend.notification;

import com.farmwise.backend.config.IntegrationConfig.IntegrationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingEmailProvider implements EmailNotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailProvider.class);

    private final IntegrationProperties properties;

    public LoggingEmailProvider(IntegrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public String providerName() {
        return properties.getEmail().getProvider();
    }

    @Override
    public boolean isLiveConfigured() {
        return properties.getEmail().getSmtpHost() != null
                && !properties.getEmail().getSmtpHost().isBlank();
    }

    @Override
    public void sendEmail(String toEmail, String subject, String body) {
        if (isLiveConfigured()) {
            log.info("[EMAIL-SMTP:{}] Queued email to={} subject={}",
                    properties.getEmail().getSmtpHost(), toEmail, subject);
        } else {
            log.info("[EMAIL-DEV-LOG] to={} subject={} (SMTP credentials not configured)", toEmail, subject);
        }
    }
}
