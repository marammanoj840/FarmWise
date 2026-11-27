package com.farmwise.backend.admin;

import com.farmwise.backend.admin.AdminDtos.AuditLogItem;
import com.farmwise.backend.admin.AdminDtos.BroadcastNotificationRequest;
import com.farmwise.backend.admin.AdminDtos.ContactInquiryUpdateRequest;
import com.farmwise.backend.admin.AdminDtos.ModerationDecisionRequest;
import com.farmwise.backend.admin.AdminDtos.PlatformStatsResponse;
import com.farmwise.backend.admin.AdminDtos.UpdateSettingRequest;
import com.farmwise.backend.admin.AdminDtos.UserRoleUpdateRequest;
import com.farmwise.backend.admin.AdminDtos.UserStatusUpdateRequest;
import com.farmwise.backend.auth.AuthDtos.UserSummary;
import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.marketplace.MarketplaceDtos.ProduceListingResponse;
import com.farmwise.backend.marketplace.MarketplaceDtos.ProductResponse;
import com.farmwise.backend.marketplace.MarketplaceDtos.SupplierProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "Platform governance, moderation, user management, audit logs, and health overview")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Platform analytics and role summary")
    public ResponseEntity<PlatformStatsResponse> stats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/users")
    @Operation(summary = "List users with optional role filter")
    public ResponseEntity<PagedResponse<UserSummary>> users(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listUsers(role, page, size));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Activate or deactivate a user account")
    public ResponseEntity<UserSummary> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UserStatusUpdateRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, request.active(), servletRequest.getRemoteAddr()));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Assign user primary role")
    public ResponseEntity<UserSummary> updateUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody UserRoleUpdateRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(adminService.updateUserRole(id, request.role(), servletRequest.getRemoteAddr()));
    }

    @PatchMapping("/experts/{expertId}/verify")
    @Operation(summary = "Approve or reject agricultural expert verification")
    public ResponseEntity<MessageResponse> verifyExpert(
            @PathVariable UUID expertId,
            @Valid @RequestBody ModerationDecisionRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(adminService.verifyExpert(expertId, request, servletRequest.getRemoteAddr()));
    }

    @GetMapping("/suppliers")
    @Operation(summary = "List agricultural input suppliers")
    public ResponseEntity<List<SupplierProfileResponse>> suppliers() {
        return ResponseEntity.ok(adminService.listSuppliers());
    }

    @PatchMapping("/suppliers/{supplierId}/verify")
    @Operation(summary = "Approve or reject supplier verification")
    public ResponseEntity<MessageResponse> verifySupplier(
            @PathVariable UUID supplierId,
            @Valid @RequestBody ModerationDecisionRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(adminService.verifySupplier(supplierId, request, servletRequest.getRemoteAddr()));
    }

    @GetMapping("/products")
    @Operation(summary = "List all marketplace products for moderation")
    public ResponseEntity<PagedResponse<ProductResponse>> products(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listAllProducts(page, size));
    }

    @PatchMapping("/products/{productId}/moderate")
    @Operation(summary = "Approve or reject a marketplace product")
    public ResponseEntity<ProductResponse> moderateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody ModerationDecisionRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(adminService.moderateProduct(productId, request, servletRequest.getRemoteAddr()));
    }

    @GetMapping("/produce")
    @Operation(summary = "List all farmer produce listings for moderation")
    public ResponseEntity<PagedResponse<ProduceListingResponse>> produce(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listAllProduce(page, size));
    }

    @PatchMapping("/produce/{listingId}/moderate")
    @Operation(summary = "Approve or reject a produce listing")
    public ResponseEntity<ProduceListingResponse> moderateProduce(
            @PathVariable UUID listingId,
            @Valid @RequestBody ModerationDecisionRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(adminService.moderateProduce(listingId, request, servletRequest.getRemoteAddr()));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Query administrative and security audit logs")
    public ResponseEntity<PagedResponse<AuditLogItem>> auditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(adminService.listAuditLogs(action, page, size));
    }

    @GetMapping("/contact-inquiries")
    @Operation(summary = "List public contact inquiries")
    public ResponseEntity<PagedResponse<ContactInquiryEntity>> contactInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listContactInquiries(page, size));
    }

    @PatchMapping("/contact-inquiries/{id}")
    @Operation(summary = "Update status or notes on a contact inquiry")
    public ResponseEntity<ContactInquiryEntity> updateContactInquiry(
            @PathVariable UUID id,
            @Valid @RequestBody ContactInquiryUpdateRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(adminService.updateContactInquiry(id, request, servletRequest.getRemoteAddr()));
    }

    @PostMapping("/notifications")
    @Operation(summary = "Send targeted or broadcast platform notification")
    public ResponseEntity<MessageResponse> sendNotification(
            @Valid @RequestBody BroadcastNotificationRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(adminService.sendNotification(request, servletRequest.getRemoteAddr()));
    }

    @GetMapping("/settings")
    @Operation(summary = "List platform system settings")
    public ResponseEntity<List<SystemSettingEntity>> settings() {
        return ResponseEntity.ok(adminService.listSettings());
    }

    @PutMapping("/settings/{key}")
    @Operation(summary = "Update a platform system setting")
    public ResponseEntity<SystemSettingEntity> updateSetting(
            @PathVariable String key,
            @Valid @RequestBody UpdateSettingRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(adminService.updateSetting(key, request.value(), servletRequest.getRemoteAddr()));
    }

    @GetMapping("/system-health")
    @Operation(summary = "Get database health and integration configuration status")
    public ResponseEntity<Map<String, Object>> systemHealth() {
        return ResponseEntity.ok(adminService.systemHealthOverview());
    }
}
