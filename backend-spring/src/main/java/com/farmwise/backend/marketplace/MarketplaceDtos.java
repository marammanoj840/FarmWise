package com.farmwise.backend.marketplace;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class MarketplaceDtos {

    private MarketplaceDtos() {
    }

    public record CategoryResponse(UUID id, String name, String slug, String description, String iconName) {
    }

    public record ProductResponse(
            UUID id,
            UUID supplierId,
            String supplierName,
            boolean supplierVerified,
            UUID categoryId,
            String categoryName,
            String name,
            String slug,
            String description,
            String unit,
            BigDecimal priceInr,
            int stockQuantity,
            String brand,
            String imageUrl,
            String approvalStatus,
            boolean active,
            OffsetDateTime createdAt) {
    }

    public record ProductUpsertRequest(
            @NotNull UUID categoryId,
            @NotBlank @Size(max = 180) String name,
            @NotBlank @Size(max = 4000) String description,
            @NotBlank @Size(max = 32) String unit,
            @NotNull @DecimalMin("0.01") BigDecimal priceInr,
            @Min(0) int stockQuantity,
            @Size(max = 100) String brand,
            @Size(max = 500) String imageUrl) {
    }

    public record SupplierProfileResponse(
            UUID id,
            UUID userId,
            String businessName,
            String gstin,
            String description,
            String city,
            String state,
            String phone,
            String verificationStatus) {
    }

    public record SupplierProfileUpdateRequest(
            @NotBlank @Size(max = 160) String businessName,
            @Size(max = 32) String gstin,
            @Size(max = 2000) String description,
            @Size(max = 100) String city,
            @Size(max = 100) String state,
            @Size(max = 32) String phone) {
    }

    public record CartItemResponse(
            UUID id,
            UUID productId,
            String productName,
            String unit,
            BigDecimal unitPriceInr,
            int quantity,
            BigDecimal lineTotalInr,
            int availableStock,
            String imageUrl) {
    }

    public record CartResponse(
            UUID cartId,
            List<CartItemResponse> items,
            BigDecimal subtotalInr,
            int totalItems,
            boolean paymentGatewayConfigured,
            String paymentProvider) {
    }

    public record AddCartItemRequest(@NotNull UUID productId, @Min(1) int quantity) {
    }

    public record UpdateCartItemRequest(@Min(1) int quantity) {
    }

    public record CheckoutRequest(
            @NotBlank @Size(max = 120) String shippingName,
            @NotBlank @Size(max = 32) String shippingPhone,
            @NotBlank @Size(max = 500) String shippingAddress,
            @NotBlank @Size(max = 100) String shippingCity,
            @NotBlank @Size(max = 100) String shippingState,
            @NotBlank @Size(max = 20) String shippingPostalCode,
            @Size(max = 1000) String notes) {
    }

    public record OrderItemResponse(
            UUID id,
            UUID orderId,
            UUID productId,
            UUID supplierId,
            String productName,
            String unit,
            BigDecimal unitPriceInr,
            int quantity,
            BigDecimal lineTotalInr,
            String fulfillmentStatus) {
    }

    public record OrderResponse(
            UUID id,
            String orderNumber,
            String status,
            String paymentStatus,
            String paymentProvider,
            String paymentNotice,
            BigDecimal totalAmountInr,
            String currency,
            String shippingName,
            String shippingPhone,
            String shippingAddress,
            String shippingCity,
            String shippingState,
            String shippingPostalCode,
            String notes,
            List<OrderItemResponse> items,
            OffsetDateTime createdAt) {
    }

    public record ProduceListingRequest(
            UUID farmId,
            @NotBlank @Size(max = 120) String cropName,
            @Size(max = 120) String variety,
            @NotNull @DecimalMin("0.01") Double quantity,
            @NotBlank @Size(max = 32) String unit,
            @NotNull @DecimalMin("0.01") BigDecimal expectedPriceInr,
            @NotBlank @Size(max = 180) String location,
            @NotBlank @Size(max = 64) String qualityGrade,
            @Size(max = 2000) String qualityNotes,
            LocalDate availableFrom,
            LocalDate harvestDate,
            @Size(max = 32) String listingStatus) {
    }

    public record ProduceListingResponse(
            UUID id,
            UUID farmerId,
            String farmerName,
            UUID farmId,
            String cropName,
            String variety,
            double quantity,
            String unit,
            BigDecimal expectedPriceInr,
            String location,
            String qualityGrade,
            String qualityNotes,
            LocalDate availableFrom,
            LocalDate harvestDate,
            String listingStatus,
            String moderationStatus,
            OffsetDateTime createdAt) {
    }

    public record ProduceInquiryRequest(
            @DecimalMin("0.01") BigDecimal offeredPriceInr,
            @NotNull @DecimalMin("0.01") Double requestedQuantity,
            @NotBlank @Size(max = 2000) String message) {
    }

    public record ProduceInquiryReplyRequest(
            @NotBlank @Size(max = 24) String status,
            @NotBlank @Size(max = 2000) String farmerReply) {
    }

    public record ProduceInquiryResponse(
            UUID id,
            UUID produceListingId,
            String cropName,
            UUID buyerUserId,
            String buyerName,
            BigDecimal offeredPriceInr,
            double requestedQuantity,
            String message,
            String status,
            String farmerReply,
            OffsetDateTime createdAt) {
    }

    public record UpdateFulfillmentStatusRequest(@NotBlank String fulfillmentStatus) {
    }
}
