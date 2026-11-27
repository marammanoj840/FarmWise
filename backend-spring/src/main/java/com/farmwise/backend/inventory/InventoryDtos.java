package com.farmwise.backend.inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class InventoryDtos {

    private InventoryDtos() {
    }

    public record InventoryItemRequest(
            UUID farmId,
            @NotBlank @Size(max = 150) String name,
            @NotBlank @Size(max = 40) String category,
            @Size(max = 64) String sku,
            @NotNull @DecimalMin("0.0") Double quantity,
            @NotBlank @Size(max = 32) String unit,
            @NotNull @DecimalMin("0.0") Double reorderThreshold,
            @NotNull @DecimalMin("0.00") BigDecimal unitCostInr,
            @Size(max = 150) String supplierName,
            @Size(max = 2000) String notes) {
    }

    public record InventoryTransactionRequest(
            @NotBlank String transactionType,
            @NotNull @DecimalMin("0.01") Double quantity,
            @DecimalMin("0.00") BigDecimal unitCostInr,
            @Size(max = 255) String referenceNote,
            LocalDate transactionDate) {
    }

    public record InventoryItemResponse(
            UUID id,
            UUID farmId,
            String name,
            String category,
            String sku,
            double quantity,
            String unit,
            double reorderThreshold,
            boolean lowStock,
            BigDecimal unitCostInr,
            String supplierName,
            String notes,
            OffsetDateTime updatedAt) {
    }

    public record InventoryTransactionResponse(
            UUID id,
            UUID inventoryItemId,
            String transactionType,
            double quantityDelta,
            BigDecimal unitCostInr,
            String referenceNote,
            LocalDate transactionDate,
            OffsetDateTime createdAt) {
    }
}
