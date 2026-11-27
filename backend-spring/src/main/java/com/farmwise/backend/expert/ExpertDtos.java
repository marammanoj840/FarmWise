package com.farmwise.backend.expert;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class ExpertDtos {

    private ExpertDtos() {
    }

    public record ExpertProfileResponse(
            UUID id,
            UUID userId,
            String fullName,
            String headline,
            String specializations,
            String qualifications,
            int experienceYears,
            String languages,
            String city,
            String state,
            BigDecimal consultationFeeInr,
            String bio,
            String verificationStatus,
            boolean verified,
            OffsetDateTime verifiedAt,
            boolean available) {
    }

    public record ExpertProfileUpdateRequest(
            @NotBlank @Size(max = 180) String headline,
            @NotBlank @Size(max = 255) String specializations,
            @Size(max = 255) String qualifications,
            @Min(0) @Max(70) int experienceYears,
            @NotBlank @Size(max = 160) String languages,
            @Size(max = 100) String city,
            @Size(max = 100) String state,
            @NotNull @DecimalMin("0.00") BigDecimal consultationFeeInr,
            @Size(max = 3000) String bio,
            boolean available) {
    }

    public record ConsultationCreateRequest(
            @NotNull UUID expertId,
            UUID farmId,
            @NotBlank @Size(max = 180) String topic,
            @Size(max = 120) String cropName,
            @NotBlank @Size(max = 3000) String question,
            @Size(max = 24) String urgency) {
    }

    public record ConsultationRespondRequest(
            @NotBlank @Size(max = 24) String status,
            @NotBlank @Size(max = 4000) String expertResponse) {
    }

    public record ConsultationResponse(
            UUID id,
            UUID farmerId,
            String farmerName,
            UUID expertId,
            String expertName,
            boolean expertVerified,
            UUID farmId,
            String topic,
            String cropName,
            String question,
            String urgency,
            String status,
            String expertResponse,
            OffsetDateTime respondedAt,
            OffsetDateTime createdAt) {
    }

    public record AppointmentCreateRequest(
            UUID consultationRequestId,
            @NotNull UUID expertId,
            @NotNull OffsetDateTime scheduledStart,
            @NotNull OffsetDateTime scheduledEnd,
            @NotBlank @Size(max = 32) String meetingMode,
            @Size(max = 1000) String meetingNotes) {
    }

    public record AppointmentResponse(
            UUID id,
            UUID consultationRequestId,
            UUID farmerId,
            String farmerName,
            UUID expertId,
            String expertName,
            OffsetDateTime scheduledStart,
            OffsetDateTime scheduledEnd,
            String meetingMode,
            String meetingNotes,
            String status) {
    }
}
