package com.farmwise.backend.auth;

import com.farmwise.backend.admin.AuditLogService;
import com.farmwise.backend.auth.AuthDtos.AuthResponse;
import com.farmwise.backend.auth.AuthDtos.ForgotPasswordRequest;
import com.farmwise.backend.auth.AuthDtos.ForgotPasswordResponse;
import com.farmwise.backend.auth.AuthDtos.LoginRequest;
import com.farmwise.backend.auth.AuthDtos.RefreshRequest;
import com.farmwise.backend.auth.AuthDtos.RegisterRequest;
import com.farmwise.backend.auth.AuthDtos.ResetPasswordRequest;
import com.farmwise.backend.auth.AuthDtos.UserSummary;
import com.farmwise.backend.auth.AuthDtos.VerifyEmailRequest;
import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.TokenHashUtil;
import com.farmwise.backend.config.JwtProperties;
import com.farmwise.backend.entity.RoleEntity;
import com.farmwise.backend.exception.BusinessValidationException;
import com.farmwise.backend.exception.ConflictException;
import com.farmwise.backend.exception.UnauthorizedException;
import com.farmwise.backend.expert.ExpertEntity;
import com.farmwise.backend.expert.ExpertRepository;
import com.farmwise.backend.marketplace.BuyerEntity;
import com.farmwise.backend.marketplace.BuyerRepository;
import com.farmwise.backend.marketplace.SupplierEntity;
import com.farmwise.backend.marketplace.SupplierRepository;
import com.farmwise.backend.notification.EmailNotificationProvider;
import com.farmwise.backend.notification.NotificationPreferenceEntity;
import com.farmwise.backend.notification.NotificationPreferenceRepository;
import com.farmwise.backend.notification.NotificationService;
import com.farmwise.backend.repository.RoleRepository;
import com.farmwise.backend.security.JwtService;
import com.farmwise.backend.security.SecurityUtils;
import com.farmwise.backend.user.UserEntity;
import com.farmwise.backend.user.UserProfileEntity;
import com.farmwise.backend.user.UserProfileRepository;
import com.farmwise.backend.user.UserRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Set<String> SELF_REGISTERABLE_ROLES = Set.of("FARMER", "EXPERT", "SUPPLIER", "BUYER");

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final ExpertRepository expertRepository;
    private final SupplierRepository supplierRepository;
    private final BuyerRepository buyerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final EmailNotificationProvider emailProvider;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final Environment environment;
    private final Clock utcClock;

    public AuthService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            NotificationPreferenceRepository notificationPreferenceRepository,
            ExpertRepository expertRepository,
            SupplierRepository supplierRepository,
            BuyerRepository buyerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties,
            EmailNotificationProvider emailProvider,
            NotificationService notificationService,
            AuditLogService auditLogService,
            Environment environment,
            Clock utcClock) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.expertRepository = expertRepository;
        this.supplierRepository = supplierRepository;
        this.buyerRepository = buyerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.emailProvider = emailProvider;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.environment = environment;
        this.utcClock = utcClock;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req, String userAgent, String ipAddress) {
        String normalizedEmail = req.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("An account with this email already exists.");
        }

        String requestedRole = req.role().trim().toUpperCase();
        if (!SELF_REGISTERABLE_ROLES.contains(requestedRole)) {
            throw new BusinessValidationException(
                    "Invalid registration role. Choose FARMER, EXPERT, SUPPLIER, or BUYER.");
        }

        RoleEntity roleEntity = roleRepository.findByName(requestedRole)
                .orElseThrow(() -> new BusinessValidationException("Requested role is not initialized."));

        UserEntity user = new UserEntity();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setFullName(req.fullName().trim());
        user.setPhone(req.phone() == null ? null : req.phone().trim());
        user.setPrimaryRole(requestedRole);
        user.setActive(true);
        user.setEmailVerified(false);
        user.getRoles().add(roleEntity);
        user = userRepository.save(user);

        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(user.getId());
        profile.setVillageOrCity(req.city());
        profile.setState(req.state());
        profile.setCountry("India");
        profile.setPreferredLanguage("en");
        profile.setPreferredCurrency("INR");
        userProfileRepository.save(profile);

        NotificationPreferenceEntity prefs = new NotificationPreferenceEntity();
        prefs.setUserId(user.getId());
        notificationPreferenceRepository.save(prefs);

        initializeRoleProfile(user, req, requestedRole);

        String rawVerifyToken = createEmailVerificationToken(user);
        emailProvider.sendEmail(
                user.getEmail(),
                "Verify your FarmWise account",
                "Welcome to FarmWise! Use verification token: " + rawVerifyToken);

        notificationService.notify(
                user.getId(),
                "ACCOUNT",
                "Welcome to FarmWise",
                "Your " + requestedRole.toLowerCase() + " account is ready. Complete your profile and verify your email.",
                "/app/profile");

        auditLogService.record(
                user.getId(),
                user.getEmail(),
                "USER_REGISTERED",
                "USER",
                user.getId().toString(),
                Map.of("role", requestedRole),
                ipAddress);

        String previewToken = isNonProdAndEmailUnconfigured() ? rawVerifyToken : null;
        return issueTokensForUser(user, userAgent, ipAddress, previewToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest req, String userAgent, String ipAddress) {
        String email = req.email().trim().toLowerCase();
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }
        if (!user.isActive()) {
            throw new UnauthorizedException("This account has been deactivated. Contact platform support.");
        }

        user.setLastLoginAt(OffsetDateTime.now(utcClock));
        userRepository.save(user);

        auditLogService.record(
                user.getId(),
                user.getEmail(),
                "USER_LOGIN",
                "USER",
                user.getId().toString(),
                Map.of("role", user.getPrimaryRole()),
                ipAddress);

        return issueTokensForUser(user, userAgent, ipAddress, null);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req, String userAgent, String ipAddress) {
        String hash = TokenHashUtil.sha256(req.refreshToken().trim());
        RefreshTokenEntity existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token."));

        OffsetDateTime now = OffsetDateTime.now(utcClock);
        if (existing.getRevokedAt() != null || !existing.getExpiresAt().isAfter(now)) {
            throw new UnauthorizedException("Refresh token has expired or been revoked.");
        }

        UserEntity user = userRepository.findById(existing.getUserId())
                .orElseThrow(() -> new UnauthorizedException("Account no longer exists."));
        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated.");
        }

        String nextRawRefresh = TokenHashUtil.generateOpaqueToken(48);
        String nextJti = UUID.randomUUID().toString();

        existing.setRevokedAt(now);
        existing.setReplacedByJti(nextJti);
        refreshTokenRepository.save(existing);

        RefreshTokenEntity rotated = new RefreshTokenEntity();
        rotated.setUserId(user.getId());
        rotated.setJti(nextJti);
        rotated.setTokenHash(TokenHashUtil.sha256(nextRawRefresh));
        rotated.setExpiresAt(now.plusDays(jwtProperties.getRefreshTokenDays()));
        rotated.setUserAgent(truncate(userAgent, 255));
        rotated.setIpAddress(truncate(ipAddress, 64));
        refreshTokenRepository.save(rotated);

        return buildAuthResponse(user, nextRawRefresh, null);
    }

    @Transactional
    public MessageResponse logout(String rawRefreshToken, String ipAddress) {
        OffsetDateTime now = OffsetDateTime.now(utcClock);
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256(rawRefreshToken.trim()))
                    .ifPresent(token -> {
                        if (token.getRevokedAt() == null) {
                            token.setRevokedAt(now);
                            refreshTokenRepository.save(token);
                        }
                    });
        } else {
            SecurityUtils.currentPrincipal().ifPresent(principal -> {
                List<RefreshTokenEntity> activeTokens =
                        refreshTokenRepository.findByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(principal.getUserId());
                for (RefreshTokenEntity token : activeTokens) {
                    token.setRevokedAt(now);
                }
                refreshTokenRepository.saveAll(activeTokens);
            });
        }
        SecurityUtils.currentPrincipal().ifPresent(p -> auditLogService.record(
                p.getUserId(), p.getEmail(), "USER_LOGOUT", "USER", p.getUserId().toString(), Map.of(), ipAddress));
        return new MessageResponse("Logged out successfully.", now);
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest req) {
        String email = req.email().trim().toLowerCase();
        String previewToken = null;
        var optionalUser = userRepository.findByEmailIgnoreCase(email);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            String rawToken = TokenHashUtil.generateOpaqueToken(32);
            PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
            entity.setUserId(user.getId());
            entity.setTokenHash(TokenHashUtil.sha256(rawToken));
            entity.setExpiresAt(OffsetDateTime.now(utcClock).plusMinutes(30));
            passwordResetTokenRepository.save(entity);

            emailProvider.sendEmail(
                    user.getEmail(),
                    "FarmWise Password Reset",
                    "Use this password reset token within 30 minutes: " + rawToken);

            if (isNonProdAndEmailUnconfigured()) {
                previewToken = rawToken;
            }
        }
        return new ForgotPasswordResponse(
                "If an account with that email exists, password reset instructions have been issued.",
                previewToken);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest req, String ipAddress) {
        String hash = TokenHashUtil.sha256(req.token().trim());
        PasswordResetTokenEntity resetToken = passwordResetTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BusinessValidationException("Invalid or expired password reset token."));

        OffsetDateTime now = OffsetDateTime.now(utcClock);
        if (resetToken.getUsedAt() != null || !resetToken.getExpiresAt().isAfter(now)) {
            throw new BusinessValidationException("Password reset token has expired or already been used.");
        }

        UserEntity user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new BusinessValidationException("Account not found."));
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);

        resetToken.setUsedAt(now);
        passwordResetTokenRepository.save(resetToken);

        List<RefreshTokenEntity> activeSessions =
                refreshTokenRepository.findByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(user.getId());
        for (RefreshTokenEntity session : activeSessions) {
            session.setRevokedAt(now);
        }
        refreshTokenRepository.saveAll(activeSessions);

        auditLogService.record(
                user.getId(), user.getEmail(), "PASSWORD_RESET", "USER", user.getId().toString(), Map.of(), ipAddress);

        return new MessageResponse("Password has been reset successfully. Please sign in again.", now);
    }

    @Transactional
    public MessageResponse verifyEmail(VerifyEmailRequest req) {
        String hash = TokenHashUtil.sha256(req.token().trim());
        EmailVerificationTokenEntity token = emailVerificationTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BusinessValidationException("Invalid verification token."));

        OffsetDateTime now = OffsetDateTime.now(utcClock);
        if (token.getVerifiedAt() != null) {
            return new MessageResponse("Email is already verified.", now);
        }
        if (!token.getExpiresAt().isAfter(now)) {
            throw new BusinessValidationException("Verification token has expired. Request a new token.");
        }

        UserEntity user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BusinessValidationException("Account not found."));
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(now);
        userRepository.save(user);

        token.setVerifiedAt(now);
        emailVerificationTokenRepository.save(token);

        return new MessageResponse("Email verified successfully.", now);
    }

    @Transactional
    public ForgotPasswordResponse resendVerification() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found."));
        if (user.isEmailVerified()) {
            return new ForgotPasswordResponse("Email is already verified.", null);
        }
        String rawToken = createEmailVerificationToken(user);
        emailProvider.sendEmail(
                user.getEmail(),
                "Verify your FarmWise email",
                "Verification token: " + rawToken);
        return new ForgotPasswordResponse(
                "Verification instructions have been issued.",
                isNonProdAndEmailUnconfigured() ? rawToken : null);
    }

    @Transactional(readOnly = true)
    public UserSummary currentUser() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found."));
        return toUserSummary(user);
    }

    public static UserSummary toUserSummary(UserEntity user) {
        List<String> roles = user.getRoles().stream()
                .map(RoleEntity::getName)
                .sorted()
                .toList();
        if (roles.isEmpty() && user.getPrimaryRole() != null) {
            roles = List.of(user.getPrimaryRole());
        }
        return new UserSummary(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getPrimaryRole(),
                roles,
                user.isActive(),
                user.isEmailVerified(),
                user.getLastLoginAt());
    }

    private AuthResponse issueTokensForUser(
            UserEntity user, String userAgent, String ipAddress, String devVerifyToken) {
        OffsetDateTime now = OffsetDateTime.now(utcClock);
        String rawRefresh = TokenHashUtil.generateOpaqueToken(48);

        RefreshTokenEntity refreshEntity = new RefreshTokenEntity();
        refreshEntity.setUserId(user.getId());
        refreshEntity.setJti(UUID.randomUUID().toString());
        refreshEntity.setTokenHash(TokenHashUtil.sha256(rawRefresh));
        refreshEntity.setExpiresAt(now.plusDays(jwtProperties.getRefreshTokenDays()));
        refreshEntity.setUserAgent(truncate(userAgent, 255));
        refreshEntity.setIpAddress(truncate(ipAddress, 64));
        refreshTokenRepository.save(refreshEntity);

        return buildAuthResponse(user, rawRefresh, devVerifyToken);
    }

    private AuthResponse buildAuthResponse(UserEntity user, String rawRefresh, String devVerifyToken) {
        Set<String> roleNames = user.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());
        if (roleNames.isEmpty() && user.getPrimaryRole() != null) {
            roleNames.add(user.getPrimaryRole());
        }
        String accessToken = jwtService.generateAccessToken(user, roleNames);
        return new AuthResponse(
                "Bearer",
                accessToken,
                jwtService.getAccessTokenExpiresInSeconds(),
                rawRefresh,
                toUserSummary(user),
                devVerifyToken);
    }

    private String createEmailVerificationToken(UserEntity user) {
        String rawToken = TokenHashUtil.generateOpaqueToken(32);
        EmailVerificationTokenEntity entity = new EmailVerificationTokenEntity();
        entity.setUserId(user.getId());
        entity.setTokenHash(TokenHashUtil.sha256(rawToken));
        entity.setExpiresAt(OffsetDateTime.now(utcClock).plusHours(24));
        emailVerificationTokenRepository.save(entity);
        return rawToken;
    }

    private void initializeRoleProfile(UserEntity user, RegisterRequest req, String role) {
        switch (role) {
            case "EXPERT" -> {
                ExpertEntity expert = new ExpertEntity();
                expert.setUserId(user.getId());
                expert.setHeadline(req.organizationOrBusinessName() != null && !req.organizationOrBusinessName().isBlank()
                        ? req.organizationOrBusinessName().trim()
                        : "Agricultural Specialist");
                expert.setSpecializations(req.specializations() != null && !req.specializations().isBlank()
                        ? req.specializations().trim()
                        : "Crop Agronomy & Soil Health");
                expert.setCity(req.city());
                expert.setState(req.state());
                expert.setVerificationStatus("PENDING");
                expertRepository.save(expert);
            }
            case "SUPPLIER" -> {
                SupplierEntity supplier = new SupplierEntity();
                supplier.setUserId(user.getId());
                supplier.setBusinessName(req.organizationOrBusinessName() != null && !req.organizationOrBusinessName().isBlank()
                        ? req.organizationOrBusinessName().trim()
                        : user.getFullName() + " Agri Inputs");
                supplier.setCity(req.city());
                supplier.setState(req.state());
                supplier.setPhone(user.getPhone());
                supplier.setVerificationStatus("PENDING");
                supplierRepository.save(supplier);
            }
            case "BUYER" -> {
                BuyerEntity buyer = new BuyerEntity();
                buyer.setUserId(user.getId());
                buyer.setOrganizationName(req.organizationOrBusinessName());
                buyer.setCity(req.city());
                buyer.setState(req.state());
                buyer.setVerificationStatus("PENDING");
                buyerRepository.save(buyer);
            }
            default -> {
                // FARMER profile uses UserProfileEntity + Farms module
            }
        }
    }

    private boolean isNonProdAndEmailUnconfigured() {
        boolean isProd = Arrays.stream(environment.getActiveProfiles())
                .anyMatch("prod"::equalsIgnoreCase);
        return !isProd && !emailProvider.isLiveConfigured();
    }

    private static String truncate(String val, int maxLen) {
        if (val == null) {
            return null;
        }
        return val.length() <= maxLen ? val : val.substring(0, maxLen);
    }
}
