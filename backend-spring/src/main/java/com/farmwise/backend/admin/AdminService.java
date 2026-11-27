package com.farmwise.backend.admin;

import com.farmwise.backend.admin.AdminDtos.AuditLogItem;
import com.farmwise.backend.admin.AdminDtos.BroadcastNotificationRequest;
import com.farmwise.backend.admin.AdminDtos.ContactInquiryUpdateRequest;
import com.farmwise.backend.admin.AdminDtos.IntegrationHealthItem;
import com.farmwise.backend.admin.AdminDtos.ModerationDecisionRequest;
import com.farmwise.backend.admin.AdminDtos.PlatformStatsResponse;
import com.farmwise.backend.auth.AuthDtos.UserSummary;
import com.farmwise.backend.auth.AuthService;
import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.config.IntegrationConfig.IntegrationProperties;
import com.farmwise.backend.crop.FarmCropRepository;
import com.farmwise.backend.dto.HealthResponse;
import com.farmwise.backend.entity.RoleEntity;
import com.farmwise.backend.exception.BusinessValidationException;
import com.farmwise.backend.exception.ResourceNotFoundException;
import com.farmwise.backend.expert.ExpertEntity;
import com.farmwise.backend.expert.ExpertRepository;
import com.farmwise.backend.farm.FarmRepository;
import com.farmwise.backend.marketplace.MarketplaceDtos.ProduceListingResponse;
import com.farmwise.backend.marketplace.MarketplaceDtos.ProductResponse;
import com.farmwise.backend.marketplace.MarketplaceDtos.SupplierProfileResponse;
import com.farmwise.backend.marketplace.MarketplaceService;
import com.farmwise.backend.marketplace.OrderRepository;
import com.farmwise.backend.marketplace.ProduceListingEntity;
import com.farmwise.backend.marketplace.ProduceListingRepository;
import com.farmwise.backend.marketplace.ProductEntity;
import com.farmwise.backend.marketplace.ProductRepository;
import com.farmwise.backend.marketplace.SupplierEntity;
import com.farmwise.backend.marketplace.SupplierRepository;
import com.farmwise.backend.notification.NotificationService;
import com.farmwise.backend.repository.RoleRepository;
import com.farmwise.backend.security.FarmWiseUserPrincipal;
import com.farmwise.backend.security.SecurityUtils;
import com.farmwise.backend.service.HealthService;
import com.farmwise.backend.user.UserEntity;
import com.farmwise.backend.user.UserRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FarmRepository farmRepository;
    private final FarmCropRepository farmCropRepository;
    private final ProductRepository productRepository;
    private final ProduceListingRepository produceListingRepository;
    private final OrderRepository orderRepository;
    private final ExpertRepository expertRepository;
    private final SupplierRepository supplierRepository;
    private final ContactInquiryRepository contactInquiryRepository;
    private final SystemSettingRepository settingRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final MarketplaceService marketplaceService;
    private final HealthService healthService;
    private final IntegrationProperties integrationProperties;
    private final Clock utcClock;

    public AdminService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            FarmRepository farmRepository,
            FarmCropRepository farmCropRepository,
            ProductRepository productRepository,
            ProduceListingRepository produceListingRepository,
            OrderRepository orderRepository,
            ExpertRepository expertRepository,
            SupplierRepository supplierRepository,
            ContactInquiryRepository contactInquiryRepository,
            SystemSettingRepository settingRepository,
            AuditLogRepository auditLogRepository,
            AuditLogService auditLogService,
            NotificationService notificationService,
            MarketplaceService marketplaceService,
            HealthService healthService,
            IntegrationProperties integrationProperties,
            Clock utcClock) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.farmRepository = farmRepository;
        this.farmCropRepository = farmCropRepository;
        this.productRepository = productRepository;
        this.produceListingRepository = produceListingRepository;
        this.orderRepository = orderRepository;
        this.expertRepository = expertRepository;
        this.supplierRepository = supplierRepository;
        this.contactInquiryRepository = contactInquiryRepository;
        this.settingRepository = settingRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
        this.marketplaceService = marketplaceService;
        this.healthService = healthService;
        this.integrationProperties = integrationProperties;
        this.utcClock = utcClock;
    }

    @Transactional(readOnly = true)
    public PlatformStatsResponse getStats() {
        Map<String, Long> byRole = new LinkedHashMap<>();
        for (String r : List.of("FARMER", "EXPERT", "SUPPLIER", "BUYER", "ADMIN")) {
            byRole.put(r, userRepository.countByPrimaryRole(r));
        }
        long openContacts = contactInquiryRepository.findAll().stream()
                .filter(c -> "OPEN".equalsIgnoreCase(c.getStatus()))
                .count();
        long activeCrops = farmCropRepository.findAll().stream()
                .filter(c -> !"HARVESTED".equalsIgnoreCase(c.getCultivationStatus())
                        && !"CANCELLED".equalsIgnoreCase(c.getCultivationStatus()))
                .count();

        return new PlatformStatsResponse(
                userRepository.count(),
                userRepository.countByActiveTrue(),
                byRole,
                farmRepository.count(),
                activeCrops,
                productRepository.count(),
                productRepository.countByApprovalStatus("PENDING"),
                produceListingRepository.countByListingStatus("ACTIVE"),
                orderRepository.count(),
                expertRepository.count(),
                supplierRepository.count(),
                openContacts);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserSummary> listUsers(String role, int page, int size) {
        PageRequest pageable = PageRequest.of(
                Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserEntity> users = (role != null && !role.isBlank())
                ? userRepository.findByPrimaryRoleIgnoreCase(role.trim().toUpperCase(), pageable)
                : userRepository.findAll(pageable);
        return PagedResponse.from(users.map(AuthService::toUserSummary));
    }

    @Transactional
    public UserSummary updateUserStatus(UUID targetUserId, boolean active, String ipAddress) {
        FarmWiseUserPrincipal actor = SecurityUtils.requireCurrentPrincipal();
        UserEntity user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (actor.getUserId().equals(targetUserId) && !active) {
            throw new BusinessValidationException("Administrators cannot deactivate their own active account.");
        }
        user.setActive(active);
        user = userRepository.save(user);
        auditLogService.record(
                actor.getUserId(),
                actor.getEmail(),
                active ? "ADMIN_USER_ACTIVATED" : "ADMIN_USER_DEACTIVATED",
                "USER",
                targetUserId.toString(),
                Map.of("active", active),
                ipAddress);
        return AuthService.toUserSummary(user);
    }

    @Transactional
    public UserSummary updateUserRole(UUID targetUserId, String rawRole, String ipAddress) {
        FarmWiseUserPrincipal actor = SecurityUtils.requireCurrentPrincipal();
        String roleName = rawRole.trim().toUpperCase();
        RoleEntity roleEntity = roleRepository.findByName(roleName)
                .orElseThrow(() -> new BusinessValidationException("Unknown role: " + roleName));
        UserEntity user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setPrimaryRole(roleName);
        user.getRoles().clear();
        user.getRoles().add(roleEntity);
        user = userRepository.save(user);

        auditLogService.record(
                actor.getUserId(),
                actor.getEmail(),
                "ADMIN_ROLE_UPDATED",
                "USER",
                targetUserId.toString(),
                Map.of("newRole", roleName),
                ipAddress);
        return AuthService.toUserSummary(user);
    }

    @Transactional
    public MessageResponse verifyExpert(UUID expertId, ModerationDecisionRequest req, String ipAddress) {
        FarmWiseUserPrincipal actor = SecurityUtils.requireCurrentPrincipal();
        ExpertEntity expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new ResourceNotFoundException("Expert not found."));
        String status = req.status().trim().toUpperCase();
        if (!List.of("APPROVED", "REJECTED", "PENDING").contains(status)) {
            throw new BusinessValidationException("Status must be APPROVED, REJECTED, or PENDING.");
        }
        OffsetDateTime now = OffsetDateTime.now(utcClock);
        expert.setVerificationStatus(status);
        if ("APPROVED".equals(status)) {
            expert.setVerifiedBy(actor.getUserId());
            expert.setVerifiedAt(now);
        } else {
            expert.setVerifiedBy(null);
            expert.setVerifiedAt(null);
        }
        expertRepository.save(expert);

        notificationService.notify(
                expert.getUserId(),
                "ACCOUNT",
                "Expert Verification " + status,
                "Your agricultural expert profile verification status is now " + status + ".",
                "/app/expert");

        auditLogService.record(
                actor.getUserId(),
                actor.getEmail(),
                "ADMIN_EXPERT_VERIFICATION",
                "EXPERT",
                expertId.toString(),
                Map.of("status", status),
                ipAddress);
        return new MessageResponse("Expert verification updated to " + status + ".", now);
    }

    @Transactional(readOnly = true)
    public List<SupplierProfileResponse> listSuppliers() {
        return supplierRepository.findAll().stream()
                .map(s -> new SupplierProfileResponse(
                        s.getId(),
                        s.getUserId(),
                        s.getBusinessName(),
                        s.getGstin(),
                        s.getDescription(),
                        s.getCity(),
                        s.getState(),
                        s.getPhone(),
                        s.getVerificationStatus()))
                .toList();
    }

    @Transactional
    public MessageResponse verifySupplier(UUID supplierId, ModerationDecisionRequest req, String ipAddress) {
        FarmWiseUserPrincipal actor = SecurityUtils.requireCurrentPrincipal();
        SupplierEntity supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found."));
        String status = req.status().trim().toUpperCase();
        OffsetDateTime now = OffsetDateTime.now(utcClock);
        supplier.setVerificationStatus(status);
        supplier.setVerifiedAt("APPROVED".equals(status) ? now : null);
        supplierRepository.save(supplier);

        auditLogService.record(
                actor.getUserId(),
                actor.getEmail(),
                "ADMIN_SUPPLIER_VERIFICATION",
                "SUPPLIER",
                supplierId.toString(),
                Map.of("status", status),
                ipAddress);
        return new MessageResponse("Supplier verification updated to " + status + ".", now);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> listAllProducts(int page, int size) {
        PageRequest pageable = PageRequest.of(
                Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(Sort.Direction.DESC, "createdAt"));
        return PagedResponse.from(productRepository.findAll(pageable).map(marketplaceService::toProductResponse));
    }

    @Transactional
    public ProductResponse moderateProduct(UUID productId, ModerationDecisionRequest req, String ipAddress) {
        FarmWiseUserPrincipal actor = SecurityUtils.requireCurrentPrincipal();
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        String status = req.status().trim().toUpperCase();
        product.setApprovalStatus(status);
        product.setModerationNote(req.note());
        product = productRepository.save(product);

        auditLogService.record(
                actor.getUserId(),
                actor.getEmail(),
                "ADMIN_PRODUCT_MODERATION",
                "PRODUCT",
                productId.toString(),
                Map.of("status", status),
                ipAddress);
        return marketplaceService.toProductResponse(product);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProduceListingResponse> listAllProduce(int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<ProduceListingEntity> pageRes = produceListingRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PagedResponse.from(pageRes.map(l -> {
            String farmerName = userRepository.findById(l.getFarmerId()).map(UserEntity::getFullName).orElse("Farmer");
            return marketplaceService.toProduceResponse(l, farmerName);
        }));
    }

    @Transactional
    public ProduceListingResponse moderateProduce(UUID listingId, ModerationDecisionRequest req, String ipAddress) {
        FarmWiseUserPrincipal actor = SecurityUtils.requireCurrentPrincipal();
        ProduceListingEntity listing = produceListingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Produce listing not found."));
        String status = req.status().trim().toUpperCase();
        listing.setModerationStatus(status);
        listing = produceListingRepository.save(listing);

        auditLogService.record(
                actor.getUserId(),
                actor.getEmail(),
                "ADMIN_PRODUCE_MODERATION",
                "PRODUCE_LISTING",
                listingId.toString(),
                Map.of("status", status),
                ipAddress);
        String farmerName = userRepository.findById(listing.getFarmerId()).map(UserEntity::getFullName).orElse("Farmer");
        return marketplaceService.toProduceResponse(listing, farmerName);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogItem> listAuditLogs(String action, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<AuditLogEntity> logs = (action != null && !action.isBlank())
                ? auditLogRepository.findByActionContainingIgnoreCaseOrderByCreatedAtDesc(action.trim(), pageable)
                : auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PagedResponse.from(logs.map(l -> new AuditLogItem(
                l.getId(),
                l.getActorUserId(),
                l.getActorEmail(),
                l.getAction(),
                l.getEntityType(),
                l.getEntityId(),
                l.getDetailsJson(),
                l.getIpAddress(),
                l.getCreatedAt())));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ContactInquiryEntity> listContactInquiries(int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        return PagedResponse.from(contactInquiryRepository.findAllByOrderByCreatedAtDesc(pageable));
    }

    @Transactional
    public ContactInquiryEntity updateContactInquiry(UUID id, ContactInquiryUpdateRequest req, String ipAddress) {
        FarmWiseUserPrincipal actor = SecurityUtils.requireCurrentPrincipal();
        ContactInquiryEntity entity = contactInquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact inquiry not found."));
        entity.setStatus(req.status().trim().toUpperCase());
        entity.setAdminNotes(req.adminNotes());
        entity = contactInquiryRepository.save(entity);
        auditLogService.record(
                actor.getUserId(),
                actor.getEmail(),
                "ADMIN_CONTACT_UPDATED",
                "CONTACT_INQUIRY",
                id.toString(),
                Map.of("status", entity.getStatus()),
                ipAddress);
        return entity;
    }

    @Transactional
    public MessageResponse sendNotification(BroadcastNotificationRequest req, String ipAddress) {
        FarmWiseUserPrincipal actor = SecurityUtils.requireCurrentPrincipal();
        if (req.targetUserId() != null) {
            notificationService.notify(
                    req.targetUserId(), req.notificationType(), req.title(), req.message(), req.actionUrl());
        } else {
            for (UserEntity user : userRepository.findAll()) {
                if (user.isActive()) {
                    notificationService.notify(
                            user.getId(), req.notificationType(), req.title(), req.message(), req.actionUrl());
                }
            }
        }
        auditLogService.record(
                actor.getUserId(),
                actor.getEmail(),
                "ADMIN_NOTIFICATION_SENT",
                "NOTIFICATION",
                req.targetUserId() != null ? req.targetUserId().toString() : "ALL_ACTIVE_USERS",
                Map.of("title", req.title()),
                ipAddress);
        return new MessageResponse("Notification dispatched.", OffsetDateTime.now(utcClock));
    }

    @Transactional(readOnly = true)
    public List<SystemSettingEntity> listSettings() {
        return settingRepository.findAll();
    }

    @Transactional
    public SystemSettingEntity updateSetting(String key, String value, String ipAddress) {
        FarmWiseUserPrincipal actor = SecurityUtils.requireCurrentPrincipal();
        SystemSettingEntity setting = settingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("System setting not found."));
        setting.setSettingValue(value);
        setting = settingRepository.save(setting);
        auditLogService.record(
                actor.getUserId(),
                actor.getEmail(),
                "ADMIN_SETTING_UPDATED",
                "SYSTEM_SETTING",
                key,
                Map.of("value", value),
                ipAddress);
        return setting;
    }

    public Map<String, Object> systemHealthOverview() {
        HealthResponse coreHealth = healthService.checkHealth();
        List<IntegrationHealthItem> integrations = List.of(
                new IntegrationHealthItem(
                        "Weather",
                        integrationProperties.getWeather().getProvider(),
                        !"none".equalsIgnoreCase(integrationProperties.getWeather().getProvider()),
                        "Open-Meteo HTTP adapter"),
                new IntegrationHealthItem(
                        "AI Assistant",
                        integrationProperties.getAi().getProvider(),
                        integrationProperties.getAi().isConfigured(),
                        integrationProperties.getAi().isConfigured()
                                ? "External AI credentials configured"
                                : "Unconfigured (agronomic reference fallback active)"),
                new IntegrationHealthItem(
                        "Market Data (Agmarknet/eNAM)",
                        integrationProperties.getMarket().getProvider(),
                        integrationProperties.getMarket().isLiveConfigured(),
                        integrationProperties.getMarket().isLiveConfigured()
                                ? "Live feed configured"
                                : "Sample-labeled reference mandi dataset"),
                new IntegrationHealthItem(
                        "Payment Gateway",
                        integrationProperties.getPayment().getProvider(),
                        integrationProperties.getPayment().isConfigured(),
                        integrationProperties.getPayment().isConfigured()
                                ? "Configured"
                                : "Unconfigured (no live charges processed)"),
                new IntegrationHealthItem(
                        "Email / SMTP",
                        integrationProperties.getEmail().getProvider(),
                        !integrationProperties.getEmail().getSmtpHost().isBlank(),
                        "Email interface ready"),
                new IntegrationHealthItem(
                        "SMS / OTP",
                        integrationProperties.getSms().getProvider(),
                        !integrationProperties.getSms().getApiKey().isBlank(),
                        "SMS interface ready"),
                new IntegrationHealthItem(
                        "File Storage (S3)",
                        integrationProperties.getStorage().getProvider(),
                        !integrationProperties.getStorage().getBucket().isBlank(),
                        "Storage adapter interface ready"),
                new IntegrationHealthItem(
                        "Maps / Geolocation",
                        integrationProperties.getMaps().getProvider(),
                        true,
                        "Coordinates & Open-Meteo geocoding ready"));

        return Map.of(
                "coreHealth", coreHealth,
                "integrations", integrations);
    }
}
