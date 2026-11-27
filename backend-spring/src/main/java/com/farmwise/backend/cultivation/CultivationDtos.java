package com.farmwise.backend.cultivation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class CultivationDtos {

    private CultivationDtos() {
    }

    public record ActivityRequest(
            @NotNull UUID farmId,
            UUID farmCropId,
            @NotBlank @Size(max = 40) String stage,
            @NotBlank @Size(max = 160) String title,
            @NotNull LocalDate activityDate,
            @Size(max = 160) String inputUsed,
            Double quantity,
            @Size(max = 32) String unit,
            @DecimalMin("0.00") BigDecimal costAmount,
            @Size(max = 2000) String notes) {
    }

    public record ActivityResponse(
            UUID id,
            UUID farmId,
            UUID farmCropId,
            String stage,
            String title,
            LocalDate activityDate,
            String inputUsed,
            Double quantity,
            String unit,
            BigDecimal costAmount,
            String notes,
            OffsetDateTime createdAt) {
    }

    public record TaskRequest(
            @NotNull UUID farmId,
            UUID farmCropId,
            @NotBlank @Size(max = 40) String stage,
            @NotBlank @Size(max = 160) String title,
            @NotNull LocalDate dueDate,
            OffsetDateTime reminderAt,
            @Size(max = 24) String priority,
            @Size(max = 32) String status,
            @Size(max = 2000) String notes) {
    }

    public record TaskStatusUpdateRequest(@NotBlank String status) {
    }

    public record TaskResponse(
            UUID id,
            UUID farmId,
            UUID farmCropId,
            String stage,
            String title,
            LocalDate dueDate,
            OffsetDateTime reminderAt,
            String priority,
            String status,
            String notes,
            OffsetDateTime completedAt,
            OffsetDateTime createdAt) {
    }
}
