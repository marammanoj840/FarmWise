package com.farmwise.backend.soil;

import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.crop.CropEntity;
import com.farmwise.backend.crop.CropRepository;
import com.farmwise.backend.crop.CropService;
import com.farmwise.backend.exception.ResourceNotFoundException;
import com.farmwise.backend.farm.FarmEntity;
import com.farmwise.backend.farm.FarmRepository;
import com.farmwise.backend.farm.FarmService;
import com.farmwise.backend.security.SecurityUtils;
import com.farmwise.backend.soil.SoilDtos.CropObservationRequest;
import com.farmwise.backend.soil.SoilDtos.CropObservationResponse;
import com.farmwise.backend.soil.SoilDtos.SoilRecordRequest;
import com.farmwise.backend.soil.SoilDtos.SoilRecordResponse;
import com.farmwise.backend.soil.SoilDtos.SuitabilityCandidate;
import com.farmwise.backend.soil.SoilDtos.SuitabilityReport;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoilService {

    public static final String OBSERVATION_DISCLAIMER =
            "Field observations and automated soil/crop insights are advisory only and NOT a certified laboratory or agronomic diagnosis. "
                    + "Consult a verified agricultural expert before applying chemical treatments.";

    private final SoilRecordRepository soilRecordRepository;
    private final CropObservationRepository observationRepository;
    private final CropKnowledgeBaseRepository knowledgeRepository;
    private final CropRepository cropRepository;
    private final FarmRepository farmRepository;
    private final FarmService farmService;
    private final CropService cropService;
    private final Clock utcClock;

    public SoilService(
            SoilRecordRepository soilRecordRepository,
            CropObservationRepository observationRepository,
            CropKnowledgeBaseRepository knowledgeRepository,
            CropRepository cropRepository,
            FarmRepository farmRepository,
            FarmService farmService,
            CropService cropService,
            Clock utcClock) {
        this.soilRecordRepository = soilRecordRepository;
        this.observationRepository = observationRepository;
        this.knowledgeRepository = knowledgeRepository;
        this.cropRepository = cropRepository;
        this.farmRepository = farmRepository;
        this.farmService = farmService;
        this.cropService = cropService;
        this.utcClock = utcClock;
    }

    @Transactional(readOnly = true)
    public PagedResponse<SoilRecordResponse> listSoilRecords(int page, int size) {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<SoilRecordEntity> records = soilRecordRepository.findByFarmerIdOrderByTestDateDesc(farmerId, pageable);
        Map<UUID, String> farms = farmRepository.findByFarmerId(farmerId).stream()
                .collect(Collectors.toMap(FarmEntity::getId, FarmEntity::getName));
        return PagedResponse.from(records.map(r -> toSoilResponse(r, farms.getOrDefault(r.getFarmId(), "Farm"))));
    }

    @Transactional
    public SoilRecordResponse createSoilRecord(SoilRecordRequest req) {
        FarmEntity farm = farmService.requireOwnedFarm(req.farmId());
        SoilRecordEntity entity = new SoilRecordEntity();
        entity.setFarmId(farm.getId());
        entity.setFarmerId(farm.getFarmerId());
        entity.setSoilType(req.soilType().trim());
        entity.setPh(req.ph());
        entity.setNitrogen(req.nitrogen());
        entity.setPhosphorus(req.phosphorus());
        entity.setPotassium(req.potassium());
        entity.setOrganicCarbonPct(req.organicCarbonPct());
        entity.setMoisturePct(req.moisturePct());
        entity.setFertilityRating(req.fertilityRating());
        entity.setLabName(req.labName());
        entity.setTestDate(req.testDate());
        entity.setObservations(req.observations());
        entity = soilRecordRepository.save(entity);
        return toSoilResponse(entity, farm.getName());
    }

    @Transactional
    public MessageResponse deleteSoilRecord(UUID id) {
        SoilRecordEntity entity = soilRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Soil record not found."));
        SecurityUtils.assertOwnerOrAdmin(entity.getFarmerId(), "soil record");
        soilRecordRepository.delete(entity);
        return new MessageResponse("Soil record deleted.", OffsetDateTime.now(utcClock));
    }

    @Transactional(readOnly = true)
    public PagedResponse<CropObservationResponse> listObservations(int page, int size) {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        return PagedResponse.from(
                observationRepository.findByFarmerIdOrderByObservedAtDesc(farmerId, pageable)
                        .map(this::toObservationResponse));
    }

    @Transactional
    public CropObservationResponse createObservation(CropObservationRequest req) {
        FarmEntity farm = farmService.requireOwnedFarm(req.farmId());
        if (req.farmCropId() != null) {
            cropService.requireOwnedFarmCrop(req.farmCropId());
        }
        CropObservationEntity entity = new CropObservationEntity();
        entity.setFarmId(farm.getId());
        entity.setFarmCropId(req.farmCropId());
        entity.setFarmerId(farm.getFarmerId());
        entity.setObservationType(req.observationType().trim().toUpperCase());
        entity.setSeverity(req.severity() != null && !req.severity().isBlank()
                ? req.severity().trim().toUpperCase()
                : "LOW");
        entity.setTitle(req.title().trim());
        entity.setSymptoms(req.symptoms().trim());
        entity.setAffectedAreaPct(req.affectedAreaPct());
        entity.setRecommendedAction(req.recommendedAction());
        entity.setExpertVerified(false);
        entity.setObservedAt(req.observedAt());
        return toObservationResponse(observationRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<CropKnowledgeBaseEntity> listKnowledgeBase(String q) {
        if (q == null || q.isBlank()) {
            return knowledgeRepository.findAllByOrderByCropNameAsc();
        }
        String term = q.trim();
        return knowledgeRepository.findByCropNameContainingIgnoreCaseOrSuitableSoilTypesContainingIgnoreCase(
                term, term);
    }

    @Transactional(readOnly = true)
    public SuitabilityReport evaluateSuitability(String soilType, double ph) {
        List<CropEntity> allCrops = cropRepository.findAllByOrderByNameAsc();
        List<SuitabilityCandidate> candidates = allCrops.stream()
                .filter(c -> c.getOptimalPhMin() == null || c.getOptimalPhMax() == null
                        || (ph >= c.getOptimalPhMin() - 0.5 && ph <= c.getOptimalPhMax() + 0.5))
                .map(c -> {
                    boolean exactPh = c.getOptimalPhMin() != null && c.getOptimalPhMax() != null
                            && ph >= c.getOptimalPhMin() && ph <= c.getOptimalPhMax();
                    String note = exactPh
                            ? "pH falls within reference range; validate local climate & soil NPK before sowing."
                            : "Near boundary pH tolerance; soil amendment or local expert verification recommended.";
                    String range = (c.getOptimalPhMin() != null && c.getOptimalPhMax() != null)
                            ? c.getOptimalPhMin() + " - " + c.getOptimalPhMax()
                            : "6.0 - 7.5";
                    return new SuitabilityCandidate(
                            c.getName(),
                            c.getCategory(),
                            note,
                            c.getSeasonSuitability(),
                            range,
                            c.getWaterRequirement());
                })
                .toList();

        return new SuitabilityReport(
                soilType,
                ph,
                false,
                OBSERVATION_DISCLAIMER,
                candidates);
    }

    private SoilRecordResponse toSoilResponse(SoilRecordEntity r, String farmName) {
        return new SoilRecordResponse(
                r.getId(),
                r.getFarmId(),
                farmName,
                r.getSoilType(),
                r.getPh(),
                r.getNitrogen(),
                r.getPhosphorus(),
                r.getPotassium(),
                r.getOrganicCarbonPct(),
                r.getMoisturePct(),
                r.getFertilityRating(),
                r.getLabName(),
                r.getTestDate(),
                r.getObservations(),
                r.getCreatedAt());
    }

    private CropObservationResponse toObservationResponse(CropObservationEntity o) {
        return new CropObservationResponse(
                o.getId(),
                o.getFarmId(),
                o.getFarmCropId(),
                o.getObservationType(),
                o.getSeverity(),
                o.getTitle(),
                o.getSymptoms(),
                o.getAffectedAreaPct(),
                o.getRecommendedAction(),
                o.isExpertVerified(),
                o.getObservedAt(),
                OBSERVATION_DISCLAIMER);
    }
}
