package com.farmwise.backend.crop;

import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.crop.CropDtos.CropCatalogItem;
import com.farmwise.backend.crop.CropDtos.CropVarietyItem;
import com.farmwise.backend.crop.CropDtos.FarmCropResponse;
import com.farmwise.backend.crop.CropDtos.FarmCropUpsertRequest;
import com.farmwise.backend.exception.ResourceNotFoundException;
import com.farmwise.backend.farm.FarmEntity;
import com.farmwise.backend.farm.FarmRepository;
import com.farmwise.backend.farm.FarmService;
import com.farmwise.backend.security.SecurityUtils;
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
public class CropService {

    private final CropRepository cropRepository;
    private final CropVarietyRepository varietyRepository;
    private final FarmCropRepository farmCropRepository;
    private final FarmRepository farmRepository;
    private final FarmService farmService;
    private final Clock utcClock;

    public CropService(
            CropRepository cropRepository,
            CropVarietyRepository varietyRepository,
            FarmCropRepository farmCropRepository,
            FarmRepository farmRepository,
            FarmService farmService,
            Clock utcClock) {
        this.cropRepository = cropRepository;
        this.varietyRepository = varietyRepository;
        this.farmCropRepository = farmCropRepository;
        this.farmRepository = farmRepository;
        this.farmService = farmService;
        this.utcClock = utcClock;
    }

    @Transactional(readOnly = true)
    public List<CropCatalogItem> listCatalog() {
        List<CropEntity> crops = cropRepository.findAllByOrderByNameAsc();
        Map<UUID, List<CropVarietyItem>> byCrop = varietyRepository.findAll().stream()
                .map(this::toVarietyItem)
                .collect(Collectors.groupingBy(CropVarietyItem::cropId));
        return crops.stream()
                .map(c -> new CropCatalogItem(
                        c.getId(),
                        c.getName(),
                        c.getScientificName(),
                        c.getCategory(),
                        c.getTypicalDurationDays(),
                        c.getWaterRequirement(),
                        c.getOptimalPhMin(),
                        c.getOptimalPhMax(),
                        c.getSeasonSuitability(),
                        c.getDescription(),
                        byCrop.getOrDefault(c.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CropVarietyItem> listVarieties(UUID cropId) {
        return varietyRepository.findByCropIdOrderByNameAsc(cropId).stream()
                .map(this::toVarietyItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<FarmCropResponse> listMyCrops(UUID farmId, int page, int size) {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<FarmCropEntity> crops = farmId != null
                ? farmCropRepository.findByFarmerIdAndFarmIdOrderByCreatedAtDesc(farmerId, farmId, pageable)
                : farmCropRepository.findByFarmerIdOrderByCreatedAtDesc(farmerId, pageable);

        Map<UUID, String> farmNames = farmRepository.findByFarmerId(farmerId).stream()
                .collect(Collectors.toMap(FarmEntity::getId, FarmEntity::getName));
        return PagedResponse.from(crops.map(fc -> toResponse(fc, farmNames.getOrDefault(fc.getFarmId(), "Farm"))));
    }

    @Transactional(readOnly = true)
    public FarmCropResponse getCrop(UUID id) {
        FarmCropEntity fc = requireOwnedFarmCrop(id);
        String farmName = farmRepository.findById(fc.getFarmId()).map(FarmEntity::getName).orElse("Farm");
        return toResponse(fc, farmName);
    }

    @Transactional
    public FarmCropResponse createFarmCrop(FarmCropUpsertRequest req) {
        FarmEntity farm = farmService.requireOwnedFarm(req.farmId());
        FarmCropEntity fc = new FarmCropEntity();
        fc.setFarmId(farm.getId());
        fc.setFarmerId(farm.getFarmerId());
        applyFields(fc, req, farm.getAreaUnit());
        fc = farmCropRepository.save(fc);
        return toResponse(fc, farm.getName());
    }

    @Transactional
    public FarmCropResponse updateFarmCrop(UUID id, FarmCropUpsertRequest req) {
        FarmCropEntity fc = requireOwnedFarmCrop(id);
        FarmEntity farm = farmService.requireOwnedFarm(req.farmId());
        fc.setFarmId(farm.getId());
        applyFields(fc, req, farm.getAreaUnit());
        fc = farmCropRepository.save(fc);
        return toResponse(fc, farm.getName());
    }

    @Transactional
    public MessageResponse deleteFarmCrop(UUID id) {
        FarmCropEntity fc = requireOwnedFarmCrop(id);
        farmCropRepository.delete(fc);
        return new MessageResponse("Crop record deleted.", OffsetDateTime.now(utcClock));
    }

    public FarmCropEntity requireOwnedFarmCrop(UUID id) {
        FarmCropEntity fc = farmCropRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crop planting record not found."));
        SecurityUtils.assertOwnerOrAdmin(fc.getFarmerId(), "crop");
        return fc;
    }

    private void applyFields(FarmCropEntity fc, FarmCropUpsertRequest req, String defaultAreaUnit) {
        fc.setCropId(req.cropId());
        fc.setVarietyId(req.varietyId());
        fc.setCropName(req.cropName().trim());
        fc.setVarietyName(req.varietyName());
        fc.setSeason(req.season().trim());
        fc.setPlantedArea(req.plantedArea());
        fc.setAreaUnit(req.areaUnit() != null && !req.areaUnit().isBlank()
                ? req.areaUnit().trim().toUpperCase()
                : defaultAreaUnit);
        fc.setSowingDate(req.sowingDate());
        fc.setExpectedHarvestDate(req.expectedHarvestDate());
        fc.setActualHarvestDate(req.actualHarvestDate());
        fc.setGrowthStage(req.growthStage() != null && !req.growthStage().isBlank()
                ? req.growthStage().trim().toUpperCase()
                : "PLANNING");
        fc.setCultivationStatus(req.cultivationStatus() != null && !req.cultivationStatus().isBlank()
                ? req.cultivationStatus().trim().toUpperCase()
                : "PLANNED");
        fc.setExpectedYieldKg(req.expectedYieldKg());
        fc.setActualYieldKg(req.actualYieldKg());
        fc.setFieldObservations(req.fieldObservations());
    }

    private CropVarietyItem toVarietyItem(CropVarietyEntity v) {
        return new CropVarietyItem(
                v.getId(),
                v.getCropId(),
                v.getName(),
                v.getMaturityDays(),
                v.getExpectedYieldPerAcreKg(),
                v.getClimateNotes(),
                v.getDiseaseResistance());
    }

    private FarmCropResponse toResponse(FarmCropEntity fc, String farmName) {
        return new FarmCropResponse(
                fc.getId(),
                fc.getFarmId(),
                farmName,
                fc.getFarmerId(),
                fc.getCropId(),
                fc.getVarietyId(),
                fc.getCropName(),
                fc.getVarietyName(),
                fc.getSeason(),
                fc.getPlantedArea(),
                fc.getAreaUnit(),
                fc.getSowingDate(),
                fc.getExpectedHarvestDate(),
                fc.getActualHarvestDate(),
                fc.getGrowthStage(),
                fc.getCultivationStatus(),
                fc.getExpectedYieldKg(),
                fc.getActualYieldKg(),
                fc.getFieldObservations(),
                fc.getCreatedAt(),
                fc.getUpdatedAt());
    }
}
