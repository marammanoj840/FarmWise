package com.farmwise.backend.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<ValidationErrorItem> fieldErrors) {

    public record ValidationErrorItem(String field, String message) {
    }
}
