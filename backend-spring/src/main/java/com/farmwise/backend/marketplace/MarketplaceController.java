package com.farmwise.backend.marketplace;

import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.marketplace.MarketplaceDtos.AddCartItemRequest;
import com.farmwise.backend.marketplace.MarketplaceDtos.CartResponse;
import com.farmwise.backend.marketplace.MarketplaceDtos.CategoryResponse;
import com.farmwise.backend.marketplace.MarketplaceDtos.CheckoutRequest;
import com.farmwise.backend.marketplace.MarketplaceDtos.OrderItemResponse;
import com.farmwise.backend.marketplace.MarketplaceDtos.OrderResponse;
import com.farmwise.backend.marketplace.MarketplaceDtos.ProduceInquiryReplyRequest;
import com.farmwise.backend.marketplace.MarketplaceDtos.ProduceInquiryRequest;
import com.farmwise.backend.marketplace.MarketplaceDtos.ProduceInquiryResponse;
import com.farmwise.backend.marketplace.MarketplaceDtos.ProduceListingRequest;
import com.farmwise.backend.marketplace.MarketplaceDtos.ProduceListingResponse;
import com.farmwise.backend.marketplace.MarketplaceDtos.ProductResponse;
import com.farmwise.backend.marketplace.MarketplaceDtos.ProductUpsertRequest;
import com.farmwise.backend.marketplace.MarketplaceDtos.SupplierProfileResponse;
import com.farmwise.backend.marketplace.MarketplaceDtos.SupplierProfileUpdateRequest;
import com.farmwise.backend.marketplace.MarketplaceDtos.UpdateCartItemRequest;
import com.farmwise.backend.marketplace.MarketplaceDtos.UpdateFulfillmentStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@Tag(name = "Marketplace & Produce", description = "Agricultural input products, cart, orders, suppliers, and produce listings")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    public MarketplaceController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @GetMapping("/marketplace/categories")
    @Operation(summary = "List agricultural product categories")
    public ResponseEntity<List<CategoryResponse>> categories() {
        return ResponseEntity.ok(marketplaceService.listCategories());
    }

    @GetMapping("/products")
    @Operation(summary = "Browse approved agricultural products")
    public ResponseEntity<PagedResponse<ProductResponse>> listProducts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(marketplaceService.listPublicProducts(categoryId, q, sort, page, size));
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get product detail")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(marketplaceService.getProduct(id));
    }

    @PostMapping("/products")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    @Operation(summary = "Create a supplier product listing")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(marketplaceService.createSupplierProduct(request));
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    @Operation(summary = "Update a supplier product listing")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id, @Valid @RequestBody ProductUpsertRequest request) {
        return ResponseEntity.ok(marketplaceService.updateSupplierProduct(id, request));
    }

    @GetMapping("/marketplace/supplier/profile")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    @Operation(summary = "Get supplier profile")
    public ResponseEntity<SupplierProfileResponse> supplierProfile() {
        return ResponseEntity.ok(marketplaceService.getOrCreateSupplierProfile());
    }

    @PutMapping("/marketplace/supplier/profile")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    @Operation(summary = "Update supplier profile")
    public ResponseEntity<SupplierProfileResponse> updateSupplierProfile(
            @Valid @RequestBody SupplierProfileUpdateRequest request) {
        return ResponseEntity.ok(marketplaceService.updateSupplierProfile(request));
    }

    @GetMapping("/marketplace/supplier/products")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    @Operation(summary = "List authenticated supplier's products")
    public ResponseEntity<List<ProductResponse>> supplierProducts() {
        return ResponseEntity.ok(marketplaceService.listSupplierProducts());
    }

    @GetMapping("/marketplace/supplier/orders")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    @Operation(summary = "List incoming order items for authenticated supplier")
    public ResponseEntity<List<OrderItemResponse>> supplierOrders() {
        return ResponseEntity.ok(marketplaceService.listSupplierOrderItems());
    }

    @PatchMapping("/marketplace/supplier/orders/items/{itemId}/status")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    @Operation(summary = "Update fulfillment status for a supplier order item")
    public ResponseEntity<OrderItemResponse> updateOrderItemStatus(
            @PathVariable UUID itemId, @Valid @RequestBody UpdateFulfillmentStatusRequest request) {
        return ResponseEntity.ok(marketplaceService.updateSupplierOrderItemStatus(itemId, request.fulfillmentStatus()));
    }

    @GetMapping("/marketplace/cart")
    @Operation(summary = "Get user's shopping cart")
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(marketplaceService.getCart());
    }

    @PostMapping("/marketplace/cart/items")
    @Operation(summary = "Add product to shopping cart")
    public ResponseEntity<CartResponse> addCartItem(@Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(marketplaceService.addCartItem(request));
    }

    @PatchMapping("/marketplace/cart/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable UUID itemId, @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(marketplaceService.updateCartItem(itemId, request.quantity()));
    }

    @DeleteMapping("/marketplace/cart/items/{itemId}")
    @Operation(summary = "Remove cart item")
    public ResponseEntity<CartResponse> removeCartItem(@PathVariable UUID itemId) {
        return ResponseEntity.ok(marketplaceService.removeCartItem(itemId));
    }

    @PostMapping("/orders")
    @Operation(summary = "Create order from shopping cart")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(marketplaceService.checkout(request));
    }

    @GetMapping("/orders")
    @Operation(summary = "List user's order history")
    public ResponseEntity<PagedResponse<OrderResponse>> myOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(marketplaceService.listMyOrders(page, size));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Get order details")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(marketplaceService.getOrder(id));
    }

    @GetMapping("/produce")
    @Operation(summary = "Browse active farmer produce listings")
    public ResponseEntity<PagedResponse<ProduceListingResponse>> listProduce(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(marketplaceService.listPublicProduce(q, page, size));
    }

    @GetMapping("/produce/{id}")
    @Operation(summary = "Get produce listing detail")
    public ResponseEntity<ProduceListingResponse> getProduce(@PathVariable UUID id) {
        return ResponseEntity.ok(marketplaceService.getProduceListing(id));
    }

    @GetMapping("/marketplace/farmer/produce")
    @PreAuthorize("hasAnyRole('FARMER','ADMIN')")
    @Operation(summary = "List authenticated farmer's produce listings")
    public ResponseEntity<List<ProduceListingResponse>> myProduce() {
        return ResponseEntity.ok(marketplaceService.listMyProduceListings());
    }

    @PostMapping("/produce")
    @PreAuthorize("hasAnyRole('FARMER','ADMIN')")
    @Operation(summary = "Create a farmer produce listing")
    public ResponseEntity<ProduceListingResponse> createProduce(@Valid @RequestBody ProduceListingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(marketplaceService.createProduceListing(request));
    }

    @PutMapping("/produce/{id}")
    @PreAuthorize("hasAnyRole('FARMER','ADMIN')")
    @Operation(summary = "Update a farmer produce listing")
    public ResponseEntity<ProduceListingResponse> updateProduce(
            @PathVariable UUID id, @Valid @RequestBody ProduceListingRequest request) {
        return ResponseEntity.ok(marketplaceService.updateProduceListing(id, request));
    }

    @DeleteMapping("/produce/{id}")
    @PreAuthorize("hasAnyRole('FARMER','ADMIN')")
    @Operation(summary = "Delete a farmer produce listing")
    public ResponseEntity<MessageResponse> deleteProduce(@PathVariable UUID id) {
        return ResponseEntity.ok(marketplaceService.deleteProduceListing(id));
    }

    @PostMapping("/produce/{id}/inquiries")
    @Operation(summary = "Submit a buyer inquiry on a produce listing")
    public ResponseEntity<ProduceInquiryResponse> submitInquiry(
            @PathVariable UUID id, @Valid @RequestBody ProduceInquiryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(marketplaceService.submitProduceInquiry(id, request));
    }

    @GetMapping("/produce/{id}/inquiries")
    @Operation(summary = "List buyer inquiries for an owned produce listing")
    public ResponseEntity<List<ProduceInquiryResponse>> listingInquiries(@PathVariable UUID id) {
        return ResponseEntity.ok(marketplaceService.listInquiriesForListing(id));
    }

    @GetMapping("/marketplace/buyer/inquiries")
    @Operation(summary = "List inquiries submitted by current user")
    public ResponseEntity<List<ProduceInquiryResponse>> myInquiries() {
        return ResponseEntity.ok(marketplaceService.listMySubmittedInquiries());
    }

    @PatchMapping("/produce/inquiries/{inquiryId}/reply")
    @PreAuthorize("hasAnyRole('FARMER','ADMIN')")
    @Operation(summary = "Farmer reply to a buyer produce inquiry")
    public ResponseEntity<ProduceInquiryResponse> replyInquiry(
            @PathVariable UUID inquiryId, @Valid @RequestBody ProduceInquiryReplyRequest request) {
        return ResponseEntity.ok(marketplaceService.replyToInquiry(inquiryId, request));
    }
}
