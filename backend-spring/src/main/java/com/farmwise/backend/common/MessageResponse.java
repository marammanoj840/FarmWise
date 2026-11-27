package com.farmwise.backend.common;

import java.time.OffsetDateTime;

public record MessageResponse(String message, OffsetDateTime timestamp) {
}
