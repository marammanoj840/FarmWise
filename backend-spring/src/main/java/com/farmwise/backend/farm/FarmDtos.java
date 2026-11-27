package com.farmwise.backend.farm;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class FarmDtos {

    private FarmDtos() {
    }

    public record FarmLocationDto(
            @Size(max = 255) String addressLine,
            @NotBlank @Size(max = 120) String villageOrCity,
            @NotBlank @Size(max = 120) String district,
            @NotBlank @Size(max = 120) String state,
            @Size(max = 100) String country,
            @Size(max = 20) String postalCode,
            Double latitude,
            Double longitude,
            @Size(max = 80) String climateZone) {
    }

    public record FarmUpsertRequest(
            @NotBlank @Size(min = 2, max = 150) String name,
            @NotNull @DecimalMin(value = "0.01", message = "Land area must be positive") Double landArea,
            @NotBlank @Size(max = 24) String areaUnit,
            @NotBlank @Size(max = 64) String soilType,
            @NotBlank @Size(max = 64) String irrigationType,
            List<String> photoUrls,
            @Size(max = 2000) String description,
            @NotNull @Valid FarmLocationDto location) {
    }

    public record FarmResponse(
            UUID id,
            UUID farmerId,
            String name,
            double landArea,
            String areaUnit,
            String soilType,
            String irrigationType,
            List<String> photoUrls,
            String description,
            boolean active,
            FarmLocationDto location,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
    }
}
