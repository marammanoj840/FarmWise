package com.farmwise.backend.finance;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FinanceDtos {

    private FinanceDtos() {
    }

    public record ExpenseRequest(
            UUID farmId,
            UUID farmCropId,
            @NotBlank @Size(max = 48) String category,
            @NotBlank @Size(max = 160) String title,
            @NotNull @DecimalMin("0.01") BigDecimal amountInr,
            @NotNull LocalDate expenseDate,
            @Size(max = 140) String vendorName,
            @Size(max = 48) String paymentMethod,
            @Size(max = 2000) String notes) {
    }

    public record ExpenseResponse(
            UUID id,
            UUID farmId,
            String farmName,
            UUID farmCropId,
            String cropName,
            String category,
            String title,
            BigDecimal amountInr,
            String currency,
            LocalDate expenseDate,
            String vendorName,
            String paymentMethod,
            String notes,
            OffsetDateTime createdAt) {
    }

    public record IncomeRequest(
            UUID farmId,
            UUID farmCropId,
            @NotBlank @Size(max = 48) String category,
            @NotBlank @Size(max = 160) String title,
            @NotNull @DecimalMin("0.01") BigDecimal amountInr,
            @NotNull LocalDate incomeDate,
            @Size(max = 140) String buyerName,
            Double quantitySold,
            @Size(max = 32) String unit,
            @Size(max = 2000) String notes) {
    }

    public record IncomeResponse(
            UUID id,
            UUID farmId,
            String farmName,
            UUID farmCropId,
            String cropName,
            String category,
            String title,
            BigDecimal amountInr,
            String currency,
            LocalDate incomeDate,
            String buyerName,
            Double quantitySold,
            String unit,
            String notes,
            OffsetDateTime createdAt) {
    }

    public record FarmFinancialSummaryItem(
            UUID farmId,
            String farmName,
            BigDecimal totalIncomeInr,
            BigDecimal totalExpensesInr,
            BigDecimal netBalanceInr) {
    }

    public record CropFinancialSummaryItem(
            UUID farmCropId,
            String cropName,
            BigDecimal totalIncomeInr,
            BigDecimal totalExpensesInr,
            BigDecimal netBalanceInr) {
    }

    public record FinanceSummaryResponse(
            String currency,
            BigDecimal totalIncomeInr,
            BigDecimal totalExpensesInr,
            BigDecimal netProfitLossInr,
            Map<String, BigDecimal> expensesByCategory,
            Map<String, BigDecimal> incomeByCategory,
            List<FarmFinancialSummaryItem> byFarm,
            List<CropFinancialSummaryItem> byCrop,
            String disclaimer) {
    }
}
