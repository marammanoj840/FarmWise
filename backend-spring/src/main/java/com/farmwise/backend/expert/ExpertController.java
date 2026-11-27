package com.farmwise.backend.expert;

import com.farmwise.backend.expert.ExpertDtos.AppointmentCreateRequest;
import com.farmwise.backend.expert.ExpertDtos.AppointmentResponse;
import com.farmwise.backend.expert.ExpertDtos.ConsultationCreateRequest;
import com.farmwise.backend.expert.ExpertDtos.ConsultationRespondRequest;
import com.farmwise.backend.expert.ExpertDtos.ConsultationResponse;
import com.farmwise.backend.expert.ExpertDtos.ExpertProfileResponse;
import com.farmwise.backend.expert.ExpertDtos.ExpertProfileUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Experts & Advisory", description = "Agricultural expert directory, consultations, and appointments")
public class ExpertController {

    private final ExpertService expertService;

    public ExpertController(ExpertService expertService) {
        this.expertService = expertService;
    }

    @GetMapping("/experts")
    @Operation(summary = "Browse agricultural expert directory")
    public ResponseEntity<List<ExpertProfileResponse>> listExperts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean verifiedOnly) {
        return ResponseEntity.ok(expertService.listExperts(q, verifiedOnly));
    }

    @GetMapping("/experts/{id}")
    @Operation(summary = "Get expert profile detail")
    public ResponseEntity<ExpertProfileResponse> getExpert(@PathVariable UUID id) {
        return ResponseEntity.ok(expertService.getExpert(id));
    }

    @GetMapping("/experts/me/profile")
    @PreAuthorize("hasAnyRole('EXPERT','ADMIN')")
    @Operation(summary = "Get signed-in expert's profile")
    public ResponseEntity<ExpertProfileResponse> myProfile() {
        return ResponseEntity.ok(expertService.getMyExpertProfile());
    }

    @PutMapping("/experts/me/profile")
    @PreAuthorize("hasAnyRole('EXPERT','ADMIN')")
    @Operation(summary = "Update signed-in expert's profile")
    public ResponseEntity<ExpertProfileResponse> updateMyProfile(
            @Valid @RequestBody ExpertProfileUpdateRequest request) {
        return ResponseEntity.ok(expertService.updateMyExpertProfile(request));
    }

    @PostMapping("/consultations")
    @Operation(summary = "Submit a consultation request to an expert")
    public ResponseEntity<ConsultationResponse> createConsultation(
            @Valid @RequestBody ConsultationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expertService.createConsultation(request));
    }

    @GetMapping("/consultations/mine")
    @Operation(summary = "List consultation requests submitted by the current user")
    public ResponseEntity<List<ConsultationResponse>> myConsultations() {
        return ResponseEntity.ok(expertService.listMyFarmerConsultations());
    }

    @GetMapping("/consultations/expert/assigned")
    @PreAuthorize("hasAnyRole('EXPERT','ADMIN')")
    @Operation(summary = "List consultation requests assigned to the signed-in expert")
    public ResponseEntity<List<ConsultationResponse>> assignedConsultations() {
        return ResponseEntity.ok(expertService.listExpertAssignedConsultations());
    }

    @PatchMapping("/consultations/{id}/respond")
    @PreAuthorize("hasAnyRole('EXPERT','ADMIN')")
    @Operation(summary = "Respond to a farmer consultation request")
    public ResponseEntity<ConsultationResponse> respond(
            @PathVariable UUID id, @Valid @RequestBody ConsultationRespondRequest request) {
        return ResponseEntity.ok(expertService.respondToConsultation(id, request));
    }

    @PostMapping("/consultations/appointments")
    @Operation(summary = "Schedule an advisory appointment with an expert")
    public ResponseEntity<AppointmentResponse> scheduleAppointment(
            @Valid @RequestBody AppointmentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expertService.scheduleAppointment(request));
    }

    @GetMapping("/consultations/appointments")
    @Operation(summary = "List scheduled advisory appointments")
    public ResponseEntity<List<AppointmentResponse>> appointments() {
        return ResponseEntity.ok(expertService.listMyAppointments());
    }
}
