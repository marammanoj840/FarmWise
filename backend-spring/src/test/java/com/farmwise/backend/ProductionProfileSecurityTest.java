package com.farmwise.backend;

import com.farmwise.backend.config.CorsProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductionProfileSecurityTest {

    @Test
    void corsPropertiesFlattensCommaSeparatedOriginsFromEnvVars() {
        CorsProperties properties = new CorsProperties(List.of(
                "https://farmwise.ag, https://www.farmwise.ag",
                "https://admin.farmwise.ag"
        ));

        List<String> origins = properties.allowedOrigins();
        assertEquals(3, origins.size());
        assertTrue(origins.contains("https://farmwise.ag"));
        assertTrue(origins.contains("https://www.farmwise.ag"));
        assertTrue(origins.contains("https://admin.farmwise.ag"));
    }

    @Test
    void corsPropertiesFallsBackToLocalhostWhenEmpty() {
        CorsProperties properties = new CorsProperties(List.of());
        List<String> origins = properties.allowedOrigins();
        assertEquals(2, origins.size());
        assertTrue(origins.contains("http://localhost:5173"));
        assertTrue(origins.contains("http://127.0.0.1:5173"));
    }
}
