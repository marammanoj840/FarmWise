package com.farmwise.backend.marketplace;

import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.exception.BusinessValidationException;
import com.farmwise.backend.exception.ForbiddenOperationException;
import com.farmwise.backend.exception.ResourceNotFoundException;
import com.farmwise.backend.farm.FarmService;
import com.farmwise.backend.marketplace.MarketplaceDtos.AddCartItemRequest;
import com.farmwise.backend.marketplace.MarketplaceDtos.CartItemResponse;
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
import com.farmwise.backend.marketplace.PaymentProvider.PaymentCreationResult;
import com.farmwise.backend.notification.NotificationService;
import com.farmwise.backend.security.SecurityUtils;
import com.farmwise.backend.user.UserEntity;
import com.farmwise.backend.user.UserRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketplaceService {

    private final ProductCategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProduceListingRepository produceListingRepository;
    private final ProduceInquiryRepository produceInquiryRepository;
    private final UserRepository userRepository;
    private final FarmService farmService;
    private final PaymentProvider paymentProvider;
    private final NotificationService notificationService;
    private final Clock utcClock;

    public MarketplaceService(
            ProductCategoryRepository categoryRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProduceListingRepository produceListingRepository,
            ProduceInquiryRepository produceInquiryRepository,
            UserRepository userRepository,
            FarmService farmService,
            PaymentProvider paymentProvider,
            NotificationService notificationService,
            Clock utcClock) {
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.produceListingRepository = produceListingRepository;
        this.produceInquiryRepository = produceInquiryRepository;
        this.userRepository = userRepository;
        this.farmService = farmService;
        this.paymentProvider = paymentProvider;
        this.notificationService = notificationService;
        this.utcClock = utcClock;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getIconName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> listPublicProducts(
            UUID categoryId, String q, String sortBy, int page, int size) {
        Sort sort = switch (sortBy == null ? "" : sortBy.toLowerCase()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "priceInr");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "priceInr");
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), sort);

        Page<ProductEntity> products;
        if (q != null && !q.isBlank()) {
            products = productRepository.findByNameContainingIgnoreCaseAndApprovalStatusAndActiveTrue(
                    q.trim(), "APPROVED", pageable);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryIdAndApprovalStatusAndActiveTrue(
                    categoryId, "APPROVED", pageable);
        } else {
            products = productRepository.findByApprovalStatusAndActiveTrue("APPROVED", pageable);
        }
        return PagedResponse.from(mapProducts(products));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        return toProductResponse(product);
    }

    @Transactional
    public SupplierProfileResponse getOrCreateSupplierProfile() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        SupplierEntity s = getOrCreateSupplierEntity(userId);
        return toSupplierResponse(s);
    }

    @Transactional
    public SupplierProfileResponse updateSupplierProfile(SupplierProfileUpdateRequest req) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        SupplierEntity s = getOrCreateSupplierEntity(userId);
        s.setBusinessName(req.businessName().trim());
        s.setGstin(req.gstin());
        s.setDescription(req.description());
        s.setCity(req.city());
        s.setState(req.state());
        s.setPhone(req.phone());
        return toSupplierResponse(supplierRepository.save(s));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listSupplierProducts() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        SupplierEntity s = supplierRepository.findByUserId(userId).orElse(null);
        if (s == null) {
            return List.of();
        }
        return productRepository.findBySupplierIdOrderByCreatedAtDesc(s.getId()).stream()
                .map(this::toProductResponse)
                .toList();
    }

    @Transactional
    public ProductResponse createSupplierProduct(ProductUpsertRequest req) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        SupplierEntity supplier = getOrCreateSupplierEntity(userId);
        categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new BusinessValidationException("Invalid product category."));

        ProductEntity p = new ProductEntity();
        p.setSupplierId(supplier.getId());
        p.setCategoryId(req.categoryId());
        p.setName(req.name().trim());
        p.setSlug(generateUniqueSlug(req.name()));
        p.setDescription(req.description().trim());
        p.setUnit(req.unit().trim());
        p.setPriceInr(req.priceInr());
        p.setStockQuantity(req.stockQuantity());
        p.setBrand(req.brand());
        p.setImageUrl(req.imageUrl());
        p.setApprovalStatus("APPROVED".equalsIgnoreCase(supplier.getVerificationStatus()) ? "APPROVED" : "PENDING");
        p.setActive(true);
        return toProductResponse(productRepository.save(p));
    }

    @Transactional
    public ProductResponse updateSupplierProduct(UUID productId, ProductUpsertRequest req) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        SupplierEntity supplier = getOrCreateSupplierEntity(userId);
        ProductEntity p = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        if (!p.getSupplierId().equals(supplier.getId()) && !SecurityUtils.isAdmin()) {
            throw new ForbiddenOperationException("You do not own this product.");
        }
        categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new BusinessValidationException("Invalid product category."));
        p.setCategoryId(req.categoryId());
        p.setName(req.name().trim());
        p.setDescription(req.description().trim());
        p.setUnit(req.unit().trim());
        p.setPriceInr(req.priceInr());
        p.setStockQuantity(req.stockQuantity());
        p.setBrand(req.brand());
        p.setImageUrl(req.imageUrl());
        return toProductResponse(productRepository.save(p));
    }

    @Transactional
    public CartResponse getCart() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        CartEntity cart = getOrCreateCart(userId);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse addCartItem(AddCartItemRequest req) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        ProductEntity product = productRepository.findById(req.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        if (!product.isActive() || !"APPROVED".equalsIgnoreCase(product.getApprovalStatus())) {
            throw new BusinessValidationException("This product is not currently available for purchase.");
        }
        CartEntity cart = getOrCreateCart(userId);
        CartItemEntity item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElseGet(() -> {
                    CartItemEntity created = new CartItemEntity();
                    created.setCartId(cart.getId());
                    created.setProductId(product.getId());
                    created.setQuantity(0);
                    return created;
                });
        int desired = item.getQuantity() + req.quantity();
        if (desired > product.getStockQuantity()) {
            throw new BusinessValidationException("Only " + product.getStockQuantity() + " units in stock.");
        }
        item.setQuantity(desired);
        cartItemRepository.save(item);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItem(UUID itemId, int quantity) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        CartEntity cart = getOrCreateCart(userId);
        CartItemEntity item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found."));
        if (!item.getCartId().equals(cart.getId())) {
            throw new ForbiddenOperationException("Cart item does not belong to your cart.");
        }
        ProductEntity product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        if (quantity > product.getStockQuantity()) {
            throw new BusinessValidationException("Only " + product.getStockQuantity() + " units in stock.");
        }
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeCartItem(UUID itemId) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        CartEntity cart = getOrCreateCart(userId);
        CartItemEntity item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found."));
        if (!item.getCartId().equals(cart.getId())) {
            throw new ForbiddenOperationException("Cart item does not belong to your cart.");
        }
        cartItemRepository.delete(item);
        return buildCartResponse(cart);
    }

    @Transactional
    public OrderResponse checkout(CheckoutRequest req) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        CartEntity cart = getOrCreateCart(userId);
        List<CartItemEntity> cartItems = cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BusinessValidationException("Your shopping cart is empty.");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<ProductEntity> updatedProducts = new ArrayList<>();
        List<OrderItemEntity> orderItemsDraft = new ArrayList<>();

        for (CartItemEntity ci : cartItems) {
            ProductEntity product = productRepository.findById(ci.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product no longer exists."));
            if (!product.isActive() || !"APPROVED".equalsIgnoreCase(product.getApprovalStatus())) {
                throw new BusinessValidationException("Product '" + product.getName() + "' is unavailable.");
            }
            if (ci.getQuantity() > product.getStockQuantity()) {
                throw new BusinessValidationException(
                        "Insufficient stock for '" + product.getName() + "' (available: " + product.getStockQuantity() + ").");
            }
            product.setStockQuantity(product.getStockQuantity() - ci.getQuantity());
            updatedProducts.add(product);

            BigDecimal lineTotal = product.getPriceInr().multiply(BigDecimal.valueOf(ci.getQuantity()));
            total = total.add(lineTotal);

            OrderItemEntity oi = new OrderItemEntity();
            oi.setProductId(product.getId());
            oi.setSupplierId(product.getSupplierId());
            oi.setProductNameSnapshot(product.getName());
            oi.setUnitSnapshot(product.getUnit());
            oi.setUnitPriceInr(product.getPriceInr());
            oi.setQuantity(ci.getQuantity());
            oi.setLineTotalInr(lineTotal);
            oi.setFulfillmentStatus("PENDING");
            orderItemsDraft.add(oi);
        }

        String orderNumber = "FW-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        PaymentCreationResult paymentResult = paymentProvider.initiateOrderPayment(orderNumber, total, "INR");

        OrderEntity order = new OrderEntity();
        order.setOrderNumber(orderNumber);
        order.setUserId(userId);
        order.setStatus(paymentResult.orderStatus());
        order.setPaymentStatus(paymentResult.paymentStatus());
        order.setPaymentProvider(paymentResult.provider());
        order.setTotalAmountInr(total);
        order.setCurrency("INR");
        order.setShippingName(req.shippingName().trim());
        order.setShippingPhone(req.shippingPhone().trim());
        order.setShippingAddress(req.shippingAddress().trim());
        order.setShippingCity(req.shippingCity().trim());
        order.setShippingState(req.shippingState().trim());
        order.setShippingPostalCode(req.shippingPostalCode().trim());
        order.setNotes(req.notes());
        order = orderRepository.save(order);

        for (OrderItemEntity oi : orderItemsDraft) {
            oi.setOrderId(order.getId());
        }
        productRepository.saveAll(updatedProducts);
        List<OrderItemEntity> savedItems = orderItemRepository.saveAll(orderItemsDraft);
        cartItemRepository.deleteByCartId(cart.getId());

        notificationService.notify(
                userId,
                "ORDER_UPDATE",
                "Order Created: " + orderNumber,
                paymentResult.notice(),
                "/app/marketplace");

        return toOrderResponse(order, savedItems, paymentResult.notice());
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> listMyOrders(int page, int size) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<OrderEntity> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PagedResponse.from(orders.map(o -> toOrderResponse(o, orderItemRepository.findByOrderId(o.getId()), null)));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found."));
        SecurityUtils.assertOwnerOrAdmin(order.getUserId(), "order");
        return toOrderResponse(order, orderItemRepository.findByOrderId(orderId), null);
    }

    @Transactional(readOnly = true)
    public List<OrderItemResponse> listSupplierOrderItems() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        SupplierEntity supplier = supplierRepository.findByUserId(userId).orElse(null);
        if (supplier == null) {
            return List.of();
        }
        return orderItemRepository.findBySupplierIdOrderByCreatedAtDesc(supplier.getId()).stream()
                .map(this::toOrderItemResponse)
                .toList();
    }

    @Transactional
    public OrderItemResponse updateSupplierOrderItemStatus(UUID itemId, String status) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        SupplierEntity supplier = getOrCreateSupplierEntity(userId);
        OrderItemEntity oi = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found."));
        if (!oi.getSupplierId().equals(supplier.getId()) && !SecurityUtils.isAdmin()) {
            throw new ForbiddenOperationException("You do not own this order line item.");
        }
        oi.setFulfillmentStatus(status.trim().toUpperCase());
        oi = orderItemRepository.save(oi);
        return toOrderItemResponse(oi);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProduceListingResponse> listPublicProduce(String q, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<ProduceListingEntity> listings = (q != null && !q.isBlank())
                ? produceListingRepository.findByCropNameContainingIgnoreCaseAndListingStatusAndModerationStatusOrderByCreatedAtDesc(
                        q.trim(), "ACTIVE", "APPROVED", pageable)
                : produceListingRepository.findByListingStatusAndModerationStatusOrderByCreatedAtDesc(
                        "ACTIVE", "APPROVED", pageable);
        Map<UUID, String> userNames = userNameMap(listings.stream().map(ProduceListingEntity::getFarmerId).toList());
        return PagedResponse.from(listings.map(l -> toProduceResponse(l, userNames.getOrDefault(l.getFarmerId(), "Farmer"))));
    }

    @Transactional(readOnly = true)
    public ProduceListingResponse getProduceListing(UUID id) {
        ProduceListingEntity l = produceListingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produce listing not found."));
        String farmerName = userRepository.findById(l.getFarmerId()).map(UserEntity::getFullName).orElse("Farmer");
        return toProduceResponse(l, farmerName);
    }

    @Transactional(readOnly = true)
    public List<ProduceListingResponse> listMyProduceListings() {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        String farmerName = userRepository.findById(farmerId).map(UserEntity::getFullName).orElse("Farmer");
        return produceListingRepository.findByFarmerIdOrderByCreatedAtDesc(farmerId).stream()
                .map(l -> toProduceResponse(l, farmerName))
                .toList();
    }

    @Transactional
    public ProduceListingResponse createProduceListing(ProduceListingRequest req) {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        if (req.farmId() != null) {
            farmService.requireOwnedFarm(req.farmId());
        }
        ProduceListingEntity l = new ProduceListingEntity();
        l.setFarmerId(farmerId);
        applyProduceFields(l, req);
        l.setModerationStatus("APPROVED");
        l = produceListingRepository.save(l);
        String farmerName = userRepository.findById(farmerId).map(UserEntity::getFullName).orElse("Farmer");
        return toProduceResponse(l, farmerName);
    }

    @Transactional
    public ProduceListingResponse updateProduceListing(UUID id, ProduceListingRequest req) {
        ProduceListingEntity l = produceListingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produce listing not found."));
        SecurityUtils.assertOwnerOrAdmin(l.getFarmerId(), "produce listing");
        if (req.farmId() != null) {
            farmService.requireOwnedFarm(req.farmId());
        }
        applyProduceFields(l, req);
        l = produceListingRepository.save(l);
        String farmerName = userRepository.findById(l.getFarmerId()).map(UserEntity::getFullName).orElse("Farmer");
        return toProduceResponse(l, farmerName);
    }

    @Transactional
    public MessageResponse deleteProduceListing(UUID id) {
        ProduceListingEntity l = produceListingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produce listing not found."));
        SecurityUtils.assertOwnerOrAdmin(l.getFarmerId(), "produce listing");
        produceListingRepository.delete(l);
        return new MessageResponse("Produce listing deleted.", OffsetDateTime.now(utcClock));
    }

    @Transactional
    public ProduceInquiryResponse submitProduceInquiry(UUID listingId, ProduceInquiryRequest req) {
        UUID buyerId = SecurityUtils.requireCurrentUserId();
        ProduceListingEntity listing = produceListingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Produce listing not found."));
        ProduceInquiryEntity inquiry = new ProduceInquiryEntity();
        inquiry.setProduceListingId(listing.getId());
        inquiry.setBuyerUserId(buyerId);
        inquiry.setOfferedPriceInr(req.offeredPriceInr());
        inquiry.setRequestedQuantity(req.requestedQuantity());
        inquiry.setMessage(req.message().trim());
        inquiry.setStatus("OPEN");
        inquiry = produceInquiryRepository.save(inquiry);

        notificationService.notify(
                listing.getFarmerId(),
                "PRODUCE_INQUIRY",
                "New Buyer Inquiry: " + listing.getCropName(),
                "A buyer inquired for " + req.requestedQuantity() + " " + listing.getUnit() + " of " + listing.getCropName(),
                "/app/produce");

        String buyerName = userRepository.findById(buyerId).map(UserEntity::getFullName).orElse("Buyer");
        return new ProduceInquiryResponse(
                inquiry.getId(),
                listing.getId(),
                listing.getCropName(),
                buyerId,
                buyerName,
                inquiry.getOfferedPriceInr(),
                inquiry.getRequestedQuantity(),
                inquiry.getMessage(),
                inquiry.getStatus(),
                inquiry.getFarmerReply(),
                inquiry.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<ProduceInquiryResponse> listInquiriesForListing(UUID listingId) {
        ProduceListingEntity listing = produceListingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Produce listing not found."));
        SecurityUtils.assertOwnerOrAdmin(listing.getFarmerId(), "produce listing inquiries");
        List<ProduceInquiryEntity> list = produceInquiryRepository.findByProduceListingIdOrderByCreatedAtDesc(listingId);
        Map<UUID, String> buyers = userNameMap(list.stream().map(ProduceInquiryEntity::getBuyerUserId).toList());
        return list.stream()
                .map(inq -> new ProduceInquiryResponse(
                        inq.getId(),
                        listingId,
                        listing.getCropName(),
                        inq.getBuyerUserId(),
                        buyers.getOrDefault(inq.getBuyerUserId(), "Buyer"),
                        inq.getOfferedPriceInr(),
                        inq.getRequestedQuantity(),
                        inq.getMessage(),
                        inq.getStatus(),
                        inq.getFarmerReply(),
                        inq.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProduceInquiryResponse> listMySubmittedInquiries() {
        UUID buyerId = SecurityUtils.requireCurrentUserId();
        List<ProduceInquiryEntity> list = produceInquiryRepository.findByBuyerUserIdOrderByCreatedAtDesc(buyerId);
        String buyerName = userRepository.findById(buyerId).map(UserEntity::getFullName).orElse("Buyer");
        return list.stream().map(inq -> {
            String cropName = produceListingRepository.findById(inq.getProduceListingId())
                    .map(ProduceListingEntity::getCropName)
                    .orElse("Produce");
            return new ProduceInquiryResponse(
                    inq.getId(),
                    inq.getProduceListingId(),
                    cropName,
                    buyerId,
                    buyerName,
                    inq.getOfferedPriceInr(),
                    inq.getRequestedQuantity(),
                    inq.getMessage(),
                    inq.getStatus(),
                    inq.getFarmerReply(),
                    inq.getCreatedAt());
        }).toList();
    }

    @Transactional
    public ProduceInquiryResponse replyToInquiry(UUID inquiryId, ProduceInquiryReplyRequest req) {
        ProduceInquiryEntity inquiry = produceInquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Produce inquiry not found."));
        ProduceListingEntity listing = produceListingRepository.findById(inquiry.getProduceListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Produce listing not found."));
        SecurityUtils.assertOwnerOrAdmin(listing.getFarmerId(), "produce inquiry");
        inquiry.setStatus(req.status().trim().toUpperCase());
        inquiry.setFarmerReply(req.farmerReply().trim());
        inquiry = produceInquiryRepository.save(inquiry);

        notificationService.notify(
                inquiry.getBuyerUserId(),
                "PRODUCE_INQUIRY",
                "Farmer Replied on " + listing.getCropName(),
                req.farmerReply().trim(),
                "/app/buyer");

        String buyerName = userRepository.findById(inquiry.getBuyerUserId()).map(UserEntity::getFullName).orElse("Buyer");
        return new ProduceInquiryResponse(
                inquiry.getId(),
                listing.getId(),
                listing.getCropName(),
                inquiry.getBuyerUserId(),
                buyerName,
                inquiry.getOfferedPriceInr(),
                inquiry.getRequestedQuantity(),
                inquiry.getMessage(),
                inquiry.getStatus(),
                inquiry.getFarmerReply(),
                inquiry.getCreatedAt());
    }

    private SupplierEntity getOrCreateSupplierEntity(UUID userId) {
        return supplierRepository.findByUserId(userId).orElseGet(() -> {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found."));
            SupplierEntity s = new SupplierEntity();
            s.setUserId(userId);
            s.setBusinessName(user.getFullName() + " Agri Store");
            s.setPhone(user.getPhone());
            s.setVerificationStatus("PENDING");
            return supplierRepository.save(s);
        });
    }

    private CartEntity getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            CartEntity c = new CartEntity();
            c.setUserId(userId);
            return cartRepository.save(c);
        });
    }

    private CartResponse buildCartResponse(CartEntity cart) {
        List<CartItemEntity> items = cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId());
        List<CartItemResponse> dtos = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalQty = 0;

        for (CartItemEntity ci : items) {
            ProductEntity p = productRepository.findById(ci.getProductId()).orElse(null);
            if (p == null) {
                continue;
            }
            BigDecimal lineTotal = p.getPriceInr().multiply(BigDecimal.valueOf(ci.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            totalQty += ci.getQuantity();
            dtos.add(new CartItemResponse(
                    ci.getId(),
                    p.getId(),
                    p.getName(),
                    p.getUnit(),
                    p.getPriceInr(),
                    ci.getQuantity(),
                    lineTotal,
                    p.getStockQuantity(),
                    p.getImageUrl()));
        }

        return new CartResponse(
                cart.getId(),
                dtos,
                subtotal,
                totalQty,
                paymentProvider.isConfigured(),
                paymentProvider.providerName());
    }

    private Page<ProductResponse> mapProducts(Page<ProductEntity> products) {
        Map<UUID, SupplierEntity> suppliers = supplierRepository.findAll().stream()
                .collect(Collectors.toMap(SupplierEntity::getId, s -> s));
        Map<UUID, String> categories = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(ProductCategoryEntity::getId, ProductCategoryEntity::getName));
        return products.map(p -> toProductResponse(
                p,
                suppliers.get(p.getSupplierId()),
                categories.getOrDefault(p.getCategoryId(), "Agricultural Input")));
    }

    public ProductResponse toProductResponse(ProductEntity p) {
        SupplierEntity s = supplierRepository.findById(p.getSupplierId()).orElse(null);
        String catName = categoryRepository.findById(p.getCategoryId())
                .map(ProductCategoryEntity::getName)
                .orElse("Agricultural Input");
        return toProductResponse(p, s, catName);
    }

    private ProductResponse toProductResponse(ProductEntity p, SupplierEntity s, String categoryName) {
        return new ProductResponse(
                p.getId(),
                p.getSupplierId(),
                s != null ? s.getBusinessName() : "Agri Supplier",
                s != null && "APPROVED".equalsIgnoreCase(s.getVerificationStatus()),
                p.getCategoryId(),
                categoryName,
                p.getName(),
                p.getSlug(),
                p.getDescription(),
                p.getUnit(),
                p.getPriceInr(),
                p.getStockQuantity(),
                p.getBrand(),
                p.getImageUrl(),
                p.getApprovalStatus(),
                p.isActive(),
                p.getCreatedAt());
    }

    private SupplierProfileResponse toSupplierResponse(SupplierEntity s) {
        return new SupplierProfileResponse(
                s.getId(),
                s.getUserId(),
                s.getBusinessName(),
                s.getGstin(),
                s.getDescription(),
                s.getCity(),
                s.getState(),
                s.getPhone(),
                s.getVerificationStatus());
    }

    private OrderItemResponse toOrderItemResponse(OrderItemEntity oi) {
        return new OrderItemResponse(
                oi.getId(),
                oi.getOrderId(),
                oi.getProductId(),
                oi.getSupplierId(),
                oi.getProductNameSnapshot(),
                oi.getUnitSnapshot(),
                oi.getUnitPriceInr(),
                oi.getQuantity(),
                oi.getLineTotalInr(),
                oi.getFulfillmentStatus());
    }

    private OrderResponse toOrderResponse(OrderEntity o, List<OrderItemEntity> items, String notice) {
        String effectiveNotice = notice != null ? notice : ("UNCONFIGURED".equals(o.getPaymentStatus())
                ? "Payment gateway unconfigured at checkout; awaiting invoice/offline confirmation."
                : "Order payment status: " + o.getPaymentStatus());
        return new OrderResponse(
                o.getId(),
                o.getOrderNumber(),
                o.getStatus(),
                o.getPaymentStatus(),
                o.getPaymentProvider(),
                effectiveNotice,
                o.getTotalAmountInr(),
                o.getCurrency(),
                o.getShippingName(),
                o.getShippingPhone(),
                o.getShippingAddress(),
                o.getShippingCity(),
                o.getShippingState(),
                o.getShippingPostalCode(),
                o.getNotes(),
                items.stream().map(this::toOrderItemResponse).toList(),
                o.getCreatedAt());
    }

    private void applyProduceFields(ProduceListingEntity l, ProduceListingRequest req) {
        l.setFarmId(req.farmId());
        l.setCropName(req.cropName().trim());
        l.setVariety(req.variety());
        l.setQuantity(req.quantity());
        l.setUnit(req.unit().trim().toUpperCase());
        l.setExpectedPriceInr(req.expectedPriceInr());
        l.setLocation(req.location().trim());
        l.setQualityGrade(req.qualityGrade().trim());
        l.setQualityNotes(req.qualityNotes());
        l.setAvailableFrom(req.availableFrom());
        l.setHarvestDate(req.harvestDate());
        if (req.listingStatus() != null && !req.listingStatus().isBlank()) {
            l.setListingStatus(req.listingStatus().trim().toUpperCase());
        }
    }

    public ProduceListingResponse toProduceResponse(ProduceListingEntity l, String farmerName) {
        return new ProduceListingResponse(
                l.getId(),
                l.getFarmerId(),
                farmerName,
                l.getFarmId(),
                l.getCropName(),
                l.getVariety(),
                l.getQuantity(),
                l.getUnit(),
                l.getExpectedPriceInr(),
                l.getLocation(),
                l.getQualityGrade(),
                l.getQualityNotes(),
                l.getAvailableFrom(),
                l.getHarvestDate(),
                l.getListingStatus(),
                l.getModerationStatus(),
                l.getCreatedAt());
    }

    private Map<UUID, String> userNameMap(List<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getFullName, (a, b) -> a));
    }

    private static String generateUniqueSlug(String name) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "product";
        }
        return base + "-" + UUID.randomUUID().toString().substring(0, 6);
    }
}
