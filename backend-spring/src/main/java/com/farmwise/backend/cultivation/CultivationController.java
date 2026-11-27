package com.farmwise.backend.cultivation;

import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.cultivation.CultivationDtos.ActivityRequest;
import com.farmwise.backend.cultivation.CultivationDtos.ActivityResponse;
import com.farmwise.backend.cultivation.CultivationDtos.TaskRequest;
import com.farmwise.backend.cultivation.CultivationDtos.TaskResponse;
import com.farmwise.backend.cultivation.CultivationDtos.TaskStatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cultivation")
@Tag(name = "Cultivation", description = "Cultivation activities, task scheduling, and lifecycle tracking")
public class CultivationController {

    private final CultivationService cultivationService;

    public CultivationController(CultivationService cultivationService) {
        this.cultivationService = cultivationService;
    }

    @GetMapping("/activities")
    @Operation(summary = "List cultivation activities")
    public ResponseEntity<PagedResponse<ActivityResponse>> listActivities(
            @RequestParam(required = false) UUID farmId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(cultivationService.listActivities(farmId, page, size));
    }

    @PostMapping("/activities")
    @Operation(summary = "Log a cultivation activity")
    public ResponseEntity<ActivityResponse> createActivity(@Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cultivationService.createActivity(request));
    }

    @DeleteMapping("/activities/{id}")
    @Operation(summary = "Delete a cultivation activity")
    public ResponseEntity<MessageResponse> deleteActivity(@PathVariable UUID id) {
        return ResponseEntity.ok(cultivationService.deleteActivity(id));
    }

    @GetMapping("/tasks")
    @Operation(summary = "List cultivation tasks")
    public ResponseEntity<PagedResponse<TaskResponse>> listTasks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(cultivationService.listTasks(status, page, size));
    }

    @PostMapping("/tasks")
    @Operation(summary = "Schedule a cultivation task")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cultivationService.createTask(request));
    }

    @PatchMapping("/tasks/{id}/status")
    @Operation(summary = "Update cultivation task status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable UUID id, @Valid @RequestBody TaskStatusUpdateRequest request) {
        return ResponseEntity.ok(cultivationService.updateTaskStatus(id, request.status()));
    }

    @DeleteMapping("/tasks/{id}")
    @Operation(summary = "Delete a cultivation task")
    public ResponseEntity<MessageResponse> deleteTask(@PathVariable UUID id) {
        return ResponseEntity.ok(cultivationService.deleteTask(id));
    }
}
