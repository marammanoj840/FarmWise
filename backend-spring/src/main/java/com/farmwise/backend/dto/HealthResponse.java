package com.farmwise.backend.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record HealthResponse(
        String status,
        String service,
        String version,
        List<String> activeProfiles,
        String databaseStatus,
        OffsetDateTime timestamp) {
}
