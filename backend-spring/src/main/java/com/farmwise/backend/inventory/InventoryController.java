package com.farmwise.backend.inventory;

import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.inventory.InventoryDtos.InventoryItemRequest;
import com.farmwise.backend.inventory.InventoryDtos.InventoryItemResponse;
import com.farmwise.backend.inventory.InventoryDtos.InventoryTransactionRequest;
import com.farmwise.backend.inventory.InventoryDtos.InventoryTransactionResponse;
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
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory & Resources", description = "Seeds, fertilizers, pesticides, equipment, and stock transactions")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @Operation(summary = "List farmer inventory items")
    public ResponseEntity<PagedResponse<InventoryItemResponse>> list(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(inventoryService.list(category, page, size));
    }

    @PostMapping
    @Operation(summary = "Add an inventory item")
    public ResponseEntity<InventoryItemResponse> create(@Valid @RequestBody InventoryItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an inventory item")
    public ResponseEntity<InventoryItemResponse> update(
            @PathVariable UUID id, @Valid @RequestBody InventoryItemRequest request) {
        return ResponseEntity.ok(inventoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an inventory item")
    public ResponseEntity<MessageResponse> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.delete(id));
    }

    @PostMapping("/{id}/transactions")
    @Operation(summary = "Record a purchase or usage stock transaction")
    public ResponseEntity<InventoryItemResponse> recordTransaction(
            @PathVariable UUID id, @Valid @RequestBody InventoryTransactionRequest request) {
        return ResponseEntity.ok(inventoryService.recordTransaction(id, request));
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Get stock transaction history for an item")
    public ResponseEntity<List<InventoryTransactionResponse>> history(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.getHistory(id));
    }
}
