package com.farmwise.backend.notification;

import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.exception.ResourceNotFoundException;
import com.farmwise.backend.notification.NotificationDtos.NotificationPreferenceResponse;
import com.farmwise.backend.notification.NotificationDtos.NotificationResponse;
import com.farmwise.backend.notification.NotificationDtos.UnreadCountResponse;
import com.farmwise.backend.notification.NotificationDtos.UpdatePreferencesRequest;
import com.farmwise.backend.security.SecurityUtils;
import com.farmwise.backend.user.UserEntity;
import com.farmwise.backend.user.UserRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final EmailNotificationProvider emailProvider;
    private final SmsNotificationProvider smsProvider;
    private final Clock utcClock;

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationPreferenceRepository preferenceRepository,
            UserRepository userRepository,
            EmailNotificationProvider emailProvider,
            SmsNotificationProvider smsProvider,
            Clock utcClock) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
        this.emailProvider = emailProvider;
        this.smsProvider = smsProvider;
        this.utcClock = utcClock;
    }

    @Transactional
    public void notify(UUID userId, String type, String title, String message, String actionUrl) {
        NotificationPreferenceEntity prefs = preferenceRepository.findByUserId(userId).orElse(null);
        if (prefs == null || prefs.isInAppEnabled()) {
            NotificationEntity entity = new NotificationEntity();
            entity.setUserId(userId);
            entity.setNotificationType(type);
            entity.setTitle(title);
            entity.setMessage(message);
            entity.setActionUrl(actionUrl);
            entity.setRead(false);
            notificationRepository.save(entity);
        }
        if (prefs != null && (prefs.isEmailEnabled() || prefs.isSmsEnabled())) {
            userRepository.findById(userId).ifPresent((UserEntity user) -> {
                if (prefs.isEmailEnabled()) {
                    emailProvider.sendEmail(user.getEmail(), "[FarmWise] " + title, message);
                }
                if (prefs.isSmsEnabled() && user.getPhone() != null) {
                    smsProvider.sendSms(user.getPhone(), title + ": " + message);
                }
            });
        }
    }

    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> listMyNotifications(Boolean unreadOnly, int page, int size) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<NotificationEntity> result = Boolean.TRUE.equals(unreadOnly)
                ? notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false, pageable)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PagedResponse.from(result.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        return new UnreadCountResponse(notificationRepository.countByUserIdAndReadFalse(userId));
    }

    @Transactional
    public NotificationResponse markRead(UUID id) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        NotificationEntity entity = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found."));
        SecurityUtils.assertOwnerOrAdmin(entity.getUserId(), "notification");
        if (!entity.isRead()) {
            entity.setRead(true);
            entity.setReadAt(OffsetDateTime.now(utcClock));
            entity = notificationRepository.save(entity);
        }
        return toResponse(entity);
    }

    @Transactional
    public MessageResponse markAllRead() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        OffsetDateTime now = OffsetDateTime.now(utcClock);
        List<NotificationEntity> unread = notificationRepository.findByUserIdAndReadFalse(userId);
        for (NotificationEntity n : unread) {
            n.setRead(true);
            n.setReadAt(now);
        }
        notificationRepository.saveAll(unread);
        return new MessageResponse("All notifications marked as read.", now);
    }

    @Transactional
    public NotificationPreferenceResponse getPreferences() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        NotificationPreferenceEntity prefs = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreferenceEntity p = new NotificationPreferenceEntity();
                    p.setUserId(userId);
                    return preferenceRepository.save(p);
                });
        return toPrefResponse(prefs);
    }

    @Transactional
    public NotificationPreferenceResponse updatePreferences(UpdatePreferencesRequest req) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        NotificationPreferenceEntity prefs = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreferenceEntity p = new NotificationPreferenceEntity();
                    p.setUserId(userId);
                    return p;
                });
        prefs.setInAppEnabled(req.inAppEnabled());
        prefs.setEmailEnabled(req.emailEnabled());
        prefs.setSmsEnabled(req.smsEnabled());
        prefs.setCultivationReminders(req.cultivationReminders());
        prefs.setWeatherAlerts(req.weatherAlerts());
        prefs.setInventoryAlerts(req.inventoryAlerts());
        prefs.setOrderUpdates(req.orderUpdates());
        prefs.setExpertUpdates(req.expertUpdates());
        return toPrefResponse(preferenceRepository.save(prefs));
    }

    private NotificationResponse toResponse(NotificationEntity n) {
        return new NotificationResponse(
                n.getId(),
                n.getNotificationType(),
                n.getTitle(),
                n.getMessage(),
                n.isRead(),
                n.getReadAt(),
                n.getActionUrl(),
                n.getCreatedAt());
    }

    private NotificationPreferenceResponse toPrefResponse(NotificationPreferenceEntity p) {
        return new NotificationPreferenceResponse(
                p.isInAppEnabled(),
                p.isEmailEnabled(),
                p.isSmsEnabled(),
                p.isCultivationReminders(),
                p.isWeatherAlerts(),
                p.isInventoryAlerts(),
                p.isOrderUpdates(),
                p.isExpertUpdates(),
                emailProvider.isLiveConfigured(),
                smsProvider.isLiveConfigured());
    }
}
