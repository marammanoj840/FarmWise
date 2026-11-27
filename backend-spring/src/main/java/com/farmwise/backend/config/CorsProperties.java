package com.farmwise.backend.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            allowedOrigins = List.of("http://localhost:5173", "http://127.0.0.1:5173");
        } else {
            // Flatten comma-separated values from env vars into individual origins
            allowedOrigins = allowedOrigins.stream()
                    .flatMap(origin -> Arrays.stream(origin.split(",")))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }
}
