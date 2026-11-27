package com.farmwise.backend.farm;

import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.exception.ResourceNotFoundException;
import com.farmwise.backend.farm.FarmDtos.FarmLocationDto;
import com.farmwise.backend.farm.FarmDtos.FarmResponse;
import com.farmwise.backend.farm.FarmDtos.FarmUpsertRequest;
import com.farmwise.backend.security.SecurityUtils;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class FarmService {

    private final FarmRepository farmRepository;
    private final FarmLocationRepository locationRepository;
    private final ObjectMapper objectMapper;
    private final Clock utcClock;

    public FarmService(
            FarmRepository farmRepository,
            FarmLocationRepository locationRepository,
            ObjectMapper objectMapper,
            Clock utcClock) {
        this.farmRepository = farmRepository;
        this.locationRepository = locationRepository;
        this.objectMapper = objectMapper;
        this.utcClock = utcClock;
    }

    @Transactional(readOnly = true)
    public PagedResponse<FarmResponse> listMyFarms(String q, int page, int size) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<FarmEntity> farms = (q != null && !q.isBlank())
                ? farmRepository.findByFarmerIdAndNameContainingIgnoreCaseOrderByCreatedAtDesc(
                        userId, q.trim(), pageable)
                : farmRepository.findByFarmerIdOrderByCreatedAtDesc(userId, pageable);
        return PagedResponse.from(farms.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public FarmResponse getFarm(UUID farmId) {
        FarmEntity farm = requireOwnedFarm(farmId);
        return toResponse(farm);
    }

    @Transactional
    public FarmResponse createFarm(FarmUpsertRequest req) {
        UUID farmerId = SecurityUtils.requireCurrentUserId();
        FarmEntity farm = new FarmEntity();
        farm.setFarmerId(farmerId);
        applyFarmFields(farm, req);
        farm = farmRepository.save(farm);

        FarmLocationEntity loc = new FarmLocationEntity();
        loc.setFarmId(farm.getId());
        applyLocationFields(loc, req.location());
        loc = locationRepository.save(loc);

        return toResponse(farm, loc);
    }

    @Transactional
    public FarmResponse updateFarm(UUID farmId, FarmUpsertRequest req) {
        FarmEntity farm = requireOwnedFarm(farmId);
        applyFarmFields(farm, req);
        farm = farmRepository.save(farm);

        FarmLocationEntity loc = locationRepository.findByFarmId(farmId)
                .orElseGet(() -> {
                    FarmLocationEntity created = new FarmLocationEntity();
                    created.setFarmId(farmId);
                    return created;
                });
        applyLocationFields(loc, req.location());
        loc = locationRepository.save(loc);

        return toResponse(farm, loc);
    }

    @Transactional
    public MessageResponse deleteFarm(UUID farmId) {
        FarmEntity farm = requireOwnedFarm(farmId);
        farmRepository.delete(farm);
        return new MessageResponse("Farm deleted successfully.", OffsetDateTime.now(utcClock));
    }

    public FarmEntity requireOwnedFarm(UUID farmId) {
        FarmEntity farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found."));
        SecurityUtils.assertOwnerOrAdmin(farm.getFarmerId(), "farm");
        return farm;
    }

    private void applyFarmFields(FarmEntity farm, FarmUpsertRequest req) {
        farm.setName(req.name().trim());
        farm.setLandArea(req.landArea());
        farm.setAreaUnit(req.areaUnit().trim().toUpperCase());
        farm.setSoilType(req.soilType().trim());
        farm.setIrrigationType(req.irrigationType().trim());
        farm.setDescription(req.description());
        try {
            List<String> urls = req.photoUrls() == null ? List.of() : req.photoUrls().stream()
                    .filter(u -> u != null && !u.isBlank())
                    .map(String::trim)
                    .toList();
            farm.setPhotoUrlsJson(objectMapper.writeValueAsString(urls));
        } catch (Exception ex) {
            farm.setPhotoUrlsJson("[]");
        }
    }

    private void applyLocationFields(FarmLocationEntity loc, FarmLocationDto dto) {
        loc.setAddressLine(dto.addressLine());
        loc.setVillageOrCity(dto.villageOrCity().trim());
        loc.setDistrict(dto.district().trim());
        loc.setState(dto.state().trim());
        loc.setCountry(dto.country() != null && !dto.country().isBlank() ? dto.country().trim() : "India");
        loc.setPostalCode(dto.postalCode());
        loc.setLatitude(dto.latitude());
        loc.setLongitude(dto.longitude());
        loc.setClimateZone(dto.climateZone());
    }

    public FarmResponse toResponse(FarmEntity farm) {
        FarmLocationEntity loc = locationRepository.findByFarmId(farm.getId()).orElse(null);
        return toResponse(farm, loc);
    }

    private FarmResponse toResponse(FarmEntity farm, FarmLocationEntity loc) {
        List<String> photos = List.of();
        try {
            photos = objectMapper.readValue(farm.getPhotoUrlsJson(), new TypeReference<>() {});
        } catch (Exception ignored) {
        }
        FarmLocationDto locDto = loc == null
                ? null
                : new FarmLocationDto(
                        loc.getAddressLine(),
                        loc.getVillageOrCity(),
                        loc.getDistrict(),
                        loc.getState(),
                        loc.getCountry(),
                        loc.getPostalCode(),
                        loc.getLatitude(),
                        loc.getLongitude(),
                        loc.getClimateZone());
        return new FarmResponse(
                farm.getId(),
                farm.getFarmerId(),
                farm.getName(),
                farm.getLandArea(),
                farm.getAreaUnit(),
                farm.getSoilType(),
                farm.getIrrigationType(),
                photos,
                farm.getDescription(),
                farm.isActive(),
                locDto,
                farm.getCreatedAt(),
                farm.getUpdatedAt());
    }
}
