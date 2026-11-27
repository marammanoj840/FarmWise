package com.farmwise.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        int authRequestsPerMinute,
        int apiRequestsPerMinute) {

    public RateLimitProperties {
        if (authRequestsPerMinute <= 0) authRequestsPerMinute = 20;
        if (apiRequestsPerMinute <= 0) apiRequestsPerMinute = 120;
    }
}
