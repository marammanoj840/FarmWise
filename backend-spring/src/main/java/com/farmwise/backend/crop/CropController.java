package com.farmwise.backend.crop;

import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.crop.CropDtos.CropCatalogItem;
import com.farmwise.backend.crop.CropDtos.CropVarietyItem;
import com.farmwise.backend.crop.CropDtos.FarmCropResponse;
import com.farmwise.backend.crop.CropDtos.FarmCropUpsertRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/crops")
@Tag(name = "Crops", description = "Crop catalog, varieties, and farmer crop cycle tracking")
public class CropController {

    private final CropService cropService;

    public CropController(CropService cropService) {
        this.cropService = cropService;
    }

    @GetMapping("/catalog")
    @Operation(summary = "Browse crop reference catalog and varieties")
    public ResponseEntity<List<CropCatalogItem>> catalog() {
        return ResponseEntity.ok(cropService.listCatalog());
    }

    @GetMapping("/catalog/{cropId}/varieties")
    @Operation(summary = "List varieties for a crop")
    public ResponseEntity<List<CropVarietyItem>> varieties(@PathVariable UUID cropId) {
        return ResponseEntity.ok(cropService.listVarieties(cropId));
    }

    @GetMapping
    @Operation(summary = "List farmer crop plantings")
    public ResponseEntity<PagedResponse<FarmCropResponse>> list(
            @RequestParam(required = false) UUID farmId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(cropService.listMyCrops(farmId, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get farm crop details")
    public ResponseEntity<FarmCropResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(cropService.getCrop(id));
    }

    @PostMapping
    @Operation(summary = "Add a crop planting to a farm")
    public ResponseEntity<FarmCropResponse> create(@Valid @RequestBody FarmCropUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cropService.createFarmCrop(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update crop growth stage, dates, or harvest metrics")
    public ResponseEntity<FarmCropResponse> update(
            @PathVariable UUID id, @Valid @RequestBody FarmCropUpsertRequest request) {
        return ResponseEntity.ok(cropService.updateFarmCrop(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a crop record")
    public ResponseEntity<MessageResponse> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(cropService.deleteFarmCrop(id));
    }
}
