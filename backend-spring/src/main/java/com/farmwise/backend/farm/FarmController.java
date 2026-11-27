package com.farmwise.backend.farm;

import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.farm.FarmDtos.FarmResponse;
import com.farmwise.backend.farm.FarmDtos.FarmUpsertRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/farms")
@Tag(name = "Farms", description = "Farm registration, location, soil, and irrigation management")
public class FarmController {

    private final FarmService farmService;

    public FarmController(FarmService farmService) {
        this.farmService = farmService;
    }

    @GetMapping
    @Operation(summary = "List farms owned by the authenticated farmer")
    public ResponseEntity<PagedResponse<FarmResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(farmService.listMyFarms(q, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get farm details")
    public ResponseEntity<FarmResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(farmService.getFarm(id));
    }

    @PostMapping
    @Operation(summary = "Create a new farm")
    public ResponseEntity<FarmResponse> create(@Valid @RequestBody FarmUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmService.createFarm(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing farm")
    public ResponseEntity<FarmResponse> update(
            @PathVariable UUID id, @Valid @RequestBody FarmUpsertRequest request) {
        return ResponseEntity.ok(farmService.updateFarm(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a farm")
    public ResponseEntity<MessageResponse> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(farmService.deleteFarm(id));
    }
}
