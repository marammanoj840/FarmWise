package com.farmwise.backend.crop;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class CropDtos {

    private CropDtos() {
    }

    public record CropVarietyItem(
            UUID id,
            UUID cropId,
            String name,
            Integer maturityDays,
            Double expectedYieldPerAcreKg,
            String climateNotes,
            String diseaseResistance) {
    }

    public record CropCatalogItem(
            UUID id,
            String name,
            String scientificName,
            String category,
            Integer typicalDurationDays,
            String waterRequirement,
            Double optimalPhMin,
            Double optimalPhMax,
            String seasonSuitability,
            String description,
            List<CropVarietyItem> varieties) {
    }

    public record FarmCropUpsertRequest(
            @NotNull UUID farmId,
            UUID cropId,
            UUID varietyId,
            @NotBlank @Size(max = 120) String cropName,
            @Size(max = 120) String varietyName,
            @NotBlank @Size(max = 40) String season,
            @NotNull @DecimalMin("0.01") Double plantedArea,
            @Size(max = 24) String areaUnit,
            LocalDate sowingDate,
            LocalDate expectedHarvestDate,
            LocalDate actualHarvestDate,
            @Size(max = 40) String growthStage,
            @Size(max = 32) String cultivationStatus,
            Double expectedYieldKg,
            Double actualYieldKg,
            @Size(max = 2000) String fieldObservations) {
    }

    public record FarmCropResponse(
            UUID id,
            UUID farmId,
            String farmName,
            UUID farmerId,
            UUID cropId,
            UUID varietyId,
            String cropName,
            String varietyName,
            String season,
            double plantedArea,
            String areaUnit,
            LocalDate sowingDate,
            LocalDate expectedHarvestDate,
            LocalDate actualHarvestDate,
            String growthStage,
            String cultivationStatus,
            Double expectedYieldKg,
            Double actualYieldKg,
            String fieldObservations,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
    }
}
