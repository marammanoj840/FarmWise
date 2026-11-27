package com.farmwise.backend.soil;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class SoilDtos {

    private SoilDtos() {
    }

    public record SoilRecordRequest(
            @NotNull UUID farmId,
            @NotBlank @Size(max = 64) String soilType,
            @NotNull @DecimalMin("0.0") @DecimalMax("14.0") Double ph,
            @DecimalMin("0.0") Double nitrogen,
            @DecimalMin("0.0") Double phosphorus,
            @DecimalMin("0.0") Double potassium,
            Double organicCarbonPct,
            Double moisturePct,
            @Size(max = 40) String fertilityRating,
            @Size(max = 140) String labName,
            @NotNull LocalDate testDate,
            @Size(max = 2000) String observations) {
    }

    public record SoilRecordResponse(
            UUID id,
            UUID farmId,
            String farmName,
            String soilType,
            double ph,
            Double nitrogen,
            Double phosphorus,
            Double potassium,
            Double organicCarbonPct,
            Double moisturePct,
            String fertilityRating,
            String labName,
            LocalDate testDate,
            String observations,
            OffsetDateTime createdAt) {
    }

    public record CropObservationRequest(
            @NotNull UUID farmId,
            UUID farmCropId,
            @NotBlank @Size(max = 40) String observationType,
            @Size(max = 24) String severity,
            @NotBlank @Size(max = 160) String title,
            @NotBlank @Size(max = 2000) String symptoms,
            Double affectedAreaPct,
            @Size(max = 2000) String recommendedAction,
            @NotNull LocalDate observedAt) {
    }

    public record CropObservationResponse(
            UUID id,
            UUID farmId,
            UUID farmCropId,
            String observationType,
            String severity,
            String title,
            String symptoms,
            Double affectedAreaPct,
            String recommendedAction,
            boolean expertVerified,
            LocalDate observedAt,
            String advisoryDisclaimer) {
    }

    public record SuitabilityCandidate(
            String cropName,
            String category,
            String matchConfidenceNote,
            String seasonSuitability,
            String phRange,
            String waterRequirement) {
    }

    public record SuitabilityReport(
            String soilType,
            double ph,
            boolean professionallyVerified,
            String uncertaintyDisclaimer,
            List<SuitabilityCandidate> candidates) {
    }
}
