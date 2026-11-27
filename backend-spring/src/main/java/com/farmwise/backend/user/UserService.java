package com.farmwise.backend.user;

import com.farmwise.backend.admin.AuditLogService;
import com.farmwise.backend.auth.RefreshTokenEntity;
import com.farmwise.backend.auth.RefreshTokenRepository;
import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.entity.RoleEntity;
import com.farmwise.backend.exception.BusinessValidationException;
import com.farmwise.backend.exception.ForbiddenOperationException;
import com.farmwise.backend.exception.ResourceNotFoundException;
import com.farmwise.backend.security.SecurityUtils;
import com.farmwise.backend.user.UserDtos.ChangePasswordRequest;
import com.farmwise.backend.user.UserDtos.FullUserProfileResponse;
import com.farmwise.backend.user.UserDtos.SessionItem;
import com.farmwise.backend.user.UserDtos.UpdateProfileRequest;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final Clock utcClock;

    public UserService(
            UserRepository userRepository,
            UserProfileRepository profileRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService,
            Clock utcClock) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.utcClock = utcClock;
    }

    @Transactional(readOnly = true)
    public FullUserProfileResponse getMyProfile() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        UserProfileEntity profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> defaultProfile(userId));
        return toFullProfile(user, profile);
    }

    @Transactional
    public FullUserProfileResponse updateMyProfile(UpdateProfileRequest req) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setFullName(req.fullName().trim());
        user.setPhone(req.phone() == null ? null : req.phone().trim());
        userRepository.save(user);

        UserProfileEntity profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> defaultProfile(userId));
        profile.setAvatarUrl(req.avatarUrl());
        profile.setBio(req.bio());
        profile.setAddressLine(req.addressLine());
        profile.setVillageOrCity(req.villageOrCity());
        profile.setDistrict(req.district());
        profile.setState(req.state());
        if (req.country() != null && !req.country().isBlank()) {
            profile.setCountry(req.country().trim());
        }
        profile.setPostalCode(req.postalCode());
        if (req.preferredLanguage() != null && !req.preferredLanguage().isBlank()) {
            profile.setPreferredLanguage(req.preferredLanguage().trim());
        }
        if (req.preferredCurrency() != null && !req.preferredCurrency().isBlank()) {
            profile.setPreferredCurrency(req.preferredCurrency().trim().toUpperCase());
        }
        profile.setFarmExperienceYears(req.farmExperienceYears());
        profile = profileRepository.save(profile);

        return toFullProfile(user, profile);
    }

    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest req, String ipAddress) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new BusinessValidationException("Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);

        auditLogService.record(
                userId, user.getEmail(), "PASSWORD_CHANGED", "USER", userId.toString(), Map.of(), ipAddress);

        return new MessageResponse("Password updated successfully.", OffsetDateTime.now(utcClock));
    }

    @Transactional(readOnly = true)
    public List<SessionItem> getActiveSessions() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        OffsetDateTime now = OffsetDateTime.now(utcClock);
        return refreshTokenRepository.findByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(userId)
                .stream()
                .filter(t -> t.getExpiresAt().isAfter(now))
                .map(t -> new SessionItem(t.getId(), t.getUserAgent(), t.getIpAddress(), t.getCreatedAt(), t.getExpiresAt()))
                .toList();
    }

    @Transactional
    public MessageResponse revokeSession(UUID sessionId) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        RefreshTokenEntity token = refreshTokenRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found."));
        if (!token.getUserId().equals(userId)) {
            throw new ForbiddenOperationException("Cannot revoke another user's session.");
        }
        token.setRevokedAt(OffsetDateTime.now(utcClock));
        refreshTokenRepository.save(token);
        return new MessageResponse("Session revoked.", OffsetDateTime.now(utcClock));
    }

    @Transactional
    public MessageResponse revokeAllSessions() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        OffsetDateTime now = OffsetDateTime.now(utcClock);
        List<RefreshTokenEntity> active =
                refreshTokenRepository.findByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(userId);
        for (RefreshTokenEntity token : active) {
            token.setRevokedAt(now);
        }
        refreshTokenRepository.saveAll(active);
        return new MessageResponse("All active sessions revoked.", now);
    }

    private UserProfileEntity defaultProfile(UUID userId) {
        UserProfileEntity p = new UserProfileEntity();
        p.setUserId(userId);
        p.setCountry("India");
        p.setPreferredLanguage("en");
        p.setPreferredCurrency("INR");
        return p;
    }

    private FullUserProfileResponse toFullProfile(UserEntity user, UserProfileEntity p) {
        List<String> roles = user.getRoles().stream().map(RoleEntity::getName).sorted().toList();
        return new FullUserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getPrimaryRole(),
                roles,
                user.isActive(),
                user.isEmailVerified(),
                user.getLastLoginAt(),
                p.getAvatarUrl(),
                p.getBio(),
                p.getAddressLine(),
                p.getVillageOrCity(),
                p.getDistrict(),
                p.getState(),
                p.getCountry(),
                p.getPostalCode(),
                p.getPreferredLanguage(),
                p.getPreferredCurrency(),
                p.getFarmExperienceYears());
    }
}
