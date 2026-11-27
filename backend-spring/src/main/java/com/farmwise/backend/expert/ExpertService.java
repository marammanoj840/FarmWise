package com.farmwise.backend.expert;

import com.farmwise.backend.exception.BusinessValidationException;
import com.farmwise.backend.exception.ForbiddenOperationException;
import com.farmwise.backend.exception.ResourceNotFoundException;
import com.farmwise.backend.expert.ExpertDtos.AppointmentCreateRequest;
import com.farmwise.backend.expert.ExpertDtos.AppointmentResponse;
import com.farmwise.backend.expert.ExpertDtos.ConsultationCreateRequest;
import com.farmwise.backend.expert.ExpertDtos.ConsultationRespondRequest;
import com.farmwise.backend.expert.ExpertDtos.ConsultationResponse;
import com.farmwise.backend.expert.ExpertDtos.ExpertProfileResponse;
import com.farmwise.backend.expert.ExpertDtos.ExpertProfileUpdateRequest;
import com.farmwise.backend.farm.FarmService;
import com.farmwise.backend.notification.NotificationService;
import com.farmwise.backend.security.SecurityUtils;
import com.farmwise.backend.user.UserEntity;
import com.farmwise.backend.user.UserRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpertService {

    private final ExpertRepository expertRepository;
    private final ConsultationRequestRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final FarmService farmService;
    private final NotificationService notificationService;
    private final Clock utcClock;

    public ExpertService(
            ExpertRepository expertRepository,
            ConsultationRequestRepository consultationRepository,
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            FarmService farmService,
            NotificationService notificationService,
            Clock utcClock) {
        this.expertRepository = expertRepository;
        this.consultationRepository = consultationRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.farmService = farmService;
        this.notificationService = notificationService;
        this.utcClock = utcClock;
    }

    @Transactional(readOnly = true)
    public List<ExpertProfileResponse> listExperts(String q, Boolean verifiedOnly) {
        List<ExpertEntity> experts = (q != null && !q.isBlank())
                ? expertRepository.findBySpecializationsContainingIgnoreCaseOrHeadlineContainingIgnoreCase(
                        q.trim(), q.trim())
                : expertRepository.findAll();

        Map<UUID, String> names = userNames(experts.stream().map(ExpertEntity::getUserId).toList());
        return experts.stream()
                .filter(e -> !Boolean.TRUE.equals(verifiedOnly) || isActuallyVerified(e))
                .map(e -> toExpertResponse(e, names.getOrDefault(e.getUserId(), "Agricultural Expert")))
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpertProfileResponse getExpert(UUID expertId) {
        ExpertEntity e = expertRepository.findById(expertId)
                .orElseThrow(() -> new ResourceNotFoundException("Expert profile not found."));
        String name = userRepository.findById(e.getUserId()).map(UserEntity::getFullName).orElse("Expert");
        return toExpertResponse(e, name);
    }

    @Transactional
    public ExpertProfileResponse getMyExpertProfile() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        ExpertEntity e = getOrCreateMyExpert(userId);
        String name = userRepository.findById(userId).map(UserEntity::getFullName).orElse("Expert");
        return toExpertResponse(e, name);
    }

    @Transactional
    public ExpertProfileResponse updateMyExpertProfile(ExpertProfileUpdateRequest req) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        ExpertEntity e = getOrCreateMyExpert(userId);
        e.setHeadline(req.headline().trim());
        e.setSpecializations(req.specializations().trim());
        e.setQualifications(req.qualifications());
        e.setExperienceYears(req.experienceYears());
        e.setLanguages(req.languages().trim());
        e.setCity(req.city());
        e.setState(req.state());
        e.setConsultationFeeInr(req.consultationFeeInr());
        e.setBio(req.bio());
        e.setAvailable(req.available());
        e = expertRepository.save(e);
        String name = userRepository.findById(userId).map(UserEntity::getFullName).orElse("Expert");
        return toExpertResponse(e, name);
    }

    @Transactional
    public ConsultationResponse createConsultation(ConsultationCreateRequest req) {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        if (req.farmId() != null) {
            farmService.requireOwnedFarm(req.farmId());
        }
        ExpertEntity expert = expertRepository.findById(req.expertId())
                .orElseThrow(() -> new ResourceNotFoundException("Expert not found."));

        ConsultationRequestEntity c = new ConsultationRequestEntity();
        c.setFarmerId(farmerId);
        c.setExpertId(expert.getId());
        c.setFarmId(req.farmId());
        c.setTopic(req.topic().trim());
        c.setCropName(req.cropName());
        c.setQuestion(req.question().trim());
        c.setUrgency(req.urgency() != null && !req.urgency().isBlank() ? req.urgency().trim().toUpperCase() : "NORMAL");
        c.setStatus("PENDING");
        c = consultationRepository.save(c);

        notificationService.notify(
                expert.getUserId(),
                "EXPERT_CONSULTATION",
                "New Consultation Request: " + c.getTopic(),
                "A farmer submitted a question regarding " + (c.getCropName() != null ? c.getCropName() : "their farm") + ".",
                "/app/expert");

        return toConsultationResponse(c);
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> listMyFarmerConsultations() {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        return consultationRepository.findByFarmerIdOrderByCreatedAtDesc(farmerId).stream()
                .map(this::toConsultationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> listExpertAssignedConsultations() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        ExpertEntity expert = expertRepository.findByUserId(userId).orElse(null);
        if (expert == null) {
            return List.of();
        }
        return consultationRepository.findByExpertIdOrderByCreatedAtDesc(expert.getId()).stream()
                .map(this::toConsultationResponse)
                .toList();
    }

    @Transactional
    public ConsultationResponse respondToConsultation(UUID consultationId, ConsultationRespondRequest req) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        ConsultationRequestEntity c = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation request not found."));
        ExpertEntity expert = expertRepository.findById(c.getExpertId())
                .orElseThrow(() -> new ResourceNotFoundException("Expert not found."));
        if (!expert.getUserId().equals(userId) && !SecurityUtils.isAdmin()) {
            throw new ForbiddenOperationException("Only the assigned expert may respond to this consultation.");
        }

        c.setStatus(req.status().trim().toUpperCase());
        c.setExpertResponse(req.expertResponse().trim());
        c.setRespondedAt(OffsetDateTime.now(utcClock));
        c = consultationRepository.save(c);

        notificationService.notify(
                c.getFarmerId(),
                "EXPERT_RESPONSE",
                "Expert Response: " + c.getTopic(),
                "Your consultation request has been answered.",
                "/app/experts");

        return toConsultationResponse(c);
    }

    @Transactional
    public AppointmentResponse scheduleAppointment(AppointmentCreateRequest req) {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        if (!req.scheduledEnd().isAfter(req.scheduledStart())) {
            throw new BusinessValidationException("Appointment end time must be after start time.");
        }
        ExpertEntity expert = expertRepository.findById(req.expertId())
                .orElseThrow(() -> new ResourceNotFoundException("Expert not found."));

        AppointmentEntity appt = new AppointmentEntity();
        appt.setConsultationRequestId(req.consultationRequestId());
        appt.setFarmerId(farmerId);
        appt.setExpertId(expert.getId());
        appt.setScheduledStart(req.scheduledStart());
        appt.setScheduledEnd(req.scheduledEnd());
        appt.setMeetingMode(req.meetingMode().trim().toUpperCase());
        appt.setMeetingNotes(req.meetingNotes());
        appt.setStatus("SCHEDULED");
        appt = appointmentRepository.save(appt);

        notificationService.notify(
                expert.getUserId(),
                "EXPERT_APPOINTMENT",
                "New Advisory Appointment Scheduled",
                "An appointment has been scheduled for " + req.scheduledStart() + ".",
                "/app/expert");

        return toAppointmentResponse(appt);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listMyAppointments() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        ExpertEntity expert = expertRepository.findByUserId(userId).orElse(null);
        List<AppointmentEntity> list = expert != null
                ? appointmentRepository.findByExpertIdOrderByScheduledStartAsc(expert.getId())
                : appointmentRepository.findByFarmerIdOrderByScheduledStartAsc(userId);
        return list.stream().map(this::toAppointmentResponse).toList();
    }

    private ExpertEntity getOrCreateMyExpert(UUID userId) {
        return expertRepository.findByUserId(userId).orElseGet(() -> {
            ExpertEntity e = new ExpertEntity();
            e.setUserId(userId);
            e.setHeadline("Agricultural Specialist");
            e.setSpecializations("Crop Agronomy");
            e.setVerificationStatus("PENDING");
            return expertRepository.save(e);
        });
    }

    public static boolean isActuallyVerified(ExpertEntity e) {
        return "APPROVED".equalsIgnoreCase(e.getVerificationStatus()) && e.getVerifiedAt() != null;
    }

    public ExpertProfileResponse toExpertResponse(ExpertEntity e, String fullName) {
        return new ExpertProfileResponse(
                e.getId(),
                e.getUserId(),
                fullName,
                e.getHeadline(),
                e.getSpecializations(),
                e.getQualifications(),
                e.getExperienceYears(),
                e.getLanguages(),
                e.getCity(),
                e.getState(),
                e.getConsultationFeeInr(),
                e.getBio(),
                e.getVerificationStatus(),
                isActuallyVerified(e),
                e.getVerifiedAt(),
                e.isAvailable());
    }

    private ConsultationResponse toConsultationResponse(ConsultationRequestEntity c) {
        String farmerName = userRepository.findById(c.getFarmerId()).map(UserEntity::getFullName).orElse("Farmer");
        ExpertEntity expert = expertRepository.findById(c.getExpertId()).orElse(null);
        String expertName = expert == null
                ? "Expert"
                : userRepository.findById(expert.getUserId()).map(UserEntity::getFullName).orElse("Expert");
        return new ConsultationResponse(
                c.getId(),
                c.getFarmerId(),
                farmerName,
                c.getExpertId(),
                expertName,
                expert != null && isActuallyVerified(expert),
                c.getFarmId(),
                c.getTopic(),
                c.getCropName(),
                c.getQuestion(),
                c.getUrgency(),
                c.getStatus(),
                c.getExpertResponse(),
                c.getRespondedAt(),
                c.getCreatedAt());
    }

    private AppointmentResponse toAppointmentResponse(AppointmentEntity a) {
        String farmerName = userRepository.findById(a.getFarmerId()).map(UserEntity::getFullName).orElse("Farmer");
        ExpertEntity expert = expertRepository.findById(a.getExpertId()).orElse(null);
        String expertName = expert == null
                ? "Expert"
                : userRepository.findById(expert.getUserId()).map(UserEntity::getFullName).orElse("Expert");
        return new AppointmentResponse(
                a.getId(),
                a.getConsultationRequestId(),
                a.getFarmerId(),
                farmerName,
                a.getExpertId(),
                expertName,
                a.getScheduledStart(),
                a.getScheduledEnd(),
                a.getMeetingMode(),
                a.getMeetingNotes(),
                a.getStatus());
    }

    private Map<UUID, String> userNames(List<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getFullName, (a, b) -> a));
    }
}
