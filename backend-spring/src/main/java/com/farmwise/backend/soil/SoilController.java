package com.farmwise.backend.soil;

import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.soil.SoilDtos.CropObservationRequest;
import com.farmwise.backend.soil.SoilDtos.CropObservationResponse;
import com.farmwise.backend.soil.SoilDtos.SoilRecordRequest;
import com.farmwise.backend.soil.SoilDtos.SoilRecordResponse;
import com.farmwise.backend.soil.SoilDtos.SuitabilityReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/soil")
@Tag(name = "Soil & Crop Intelligence", description = "Soil test records, crop knowledge base, observations, and suitability reference")
public class SoilController {

    private final SoilService soilService;

    public SoilController(SoilService soilService) {
        this.soilService = soilService;
    }

    @GetMapping("/records")
    @Operation(summary = "List farmer soil test records")
    public ResponseEntity<PagedResponse<SoilRecordResponse>> listSoilRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(soilService.listSoilRecords(page, size));
    }

    @PostMapping("/records")
    @Operation(summary = "Add a soil test record for an owned farm")
    public ResponseEntity<SoilRecordResponse> createSoilRecord(@Valid @RequestBody SoilRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(soilService.createSoilRecord(request));
    }

    @DeleteMapping("/records/{id}")
    @Operation(summary = "Delete a soil test record")
    public ResponseEntity<MessageResponse> deleteSoilRecord(@PathVariable UUID id) {
        return ResponseEntity.ok(soilService.deleteSoilRecord(id));
    }

    @GetMapping("/observations")
    @Operation(summary = "List crop health, pest, and disease observations")
    public ResponseEntity<PagedResponse<CropObservationResponse>> listObservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(soilService.listObservations(page, size));
    }

    @PostMapping("/observations")
    @Operation(summary = "Log a crop health, pest, or disease observation")
    public ResponseEntity<CropObservationResponse> createObservation(
            @Valid @RequestBody CropObservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(soilService.createObservation(request));
    }

    @GetMapping("/knowledge")
    @Operation(summary = "Browse the extensible crop knowledge base")
    public ResponseEntity<List<CropKnowledgeBaseEntity>> knowledgeBase(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(soilService.listKnowledgeBase(q));
    }

    @GetMapping("/knowledge/suitability")
    @Operation(summary = "Reference crop suitability guidance by soil type and pH")
    public ResponseEntity<SuitabilityReport> suitability(
            @RequestParam(defaultValue = "Loamy") String soilType,
            @RequestParam(defaultValue = "6.8") @DecimalMin("0.0") @DecimalMax("14.0") double ph) {
        return ResponseEntity.ok(soilService.evaluateSuitability(soilType, ph));
    }
}
