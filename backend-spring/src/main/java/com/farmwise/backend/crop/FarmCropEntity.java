package com.farmwise.backend.crop;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "farm_crops")
public class FarmCropEntity extends BaseAuditEntity {

    @Column(name = "farm_id", nullable = false)
    private UUID farmId;

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "crop_id")
    private UUID cropId;

    @Column(name = "variety_id")
    private UUID varietyId;

    @Column(name = "crop_name", nullable = false, length = 120)
    private String cropName;

    @Column(name = "variety_name", length = 120)
    private String varietyName;

    @Column(name = "season", nullable = false, length = 40)
    private String season;

    @Column(name = "planted_area", nullable = false)
    private double plantedArea;

    @Column(name = "area_unit", nullable = false, length = 24)
    private String areaUnit = "ACRES";

    @Column(name = "sowing_date")
    private LocalDate sowingDate;

    @Column(name = "expected_harvest_date")
    private LocalDate expectedHarvestDate;

    @Column(name = "actual_harvest_date")
    private LocalDate actualHarvestDate;

    @Column(name = "growth_stage", nullable = false, length = 40)
    private String growthStage = "PLANNING";

    @Column(name = "cultivation_status", nullable = false, length = 32)
    private String cultivationStatus = "PLANNED";

    @Column(name = "expected_yield_kg")
    private Double expectedYieldKg;

    @Column(name = "actual_yield_kg")
    private Double actualYieldKg;

    @Column(name = "field_observations", columnDefinition = "TEXT")
    private String fieldObservations;

    public UUID getFarmId() {
        return farmId;
    }

    public void setFarmId(UUID farmId) {
        this.farmId = farmId;
    }

    public UUID getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(UUID farmerId) {
        this.farmerId = farmerId;
    }

    public UUID getCropId() {
        return cropId;
    }

    public void setCropId(UUID cropId) {
        this.cropId = cropId;
    }

    public UUID getVarietyId() {
        return varietyId;
    }

    public void setVarietyId(UUID varietyId) {
        this.varietyId = varietyId;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getVarietyName() {
        return varietyName;
    }

    public void setVarietyName(String varietyName) {
        this.varietyName = varietyName;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public double getPlantedArea() {
        return plantedArea;
    }

    public void setPlantedArea(double plantedArea) {
        this.plantedArea = plantedArea;
    }

    public String getAreaUnit() {
        return areaUnit;
    }

    public void setAreaUnit(String areaUnit) {
        this.areaUnit = areaUnit;
    }

    public LocalDate getSowingDate() {
        return sowingDate;
    }

    public void setSowingDate(LocalDate sowingDate) {
        this.sowingDate = sowingDate;
    }

    public LocalDate getExpectedHarvestDate() {
        return expectedHarvestDate;
    }

    public void setExpectedHarvestDate(LocalDate expectedHarvestDate) {
        this.expectedHarvestDate = expectedHarvestDate;
    }

    public LocalDate getActualHarvestDate() {
        return actualHarvestDate;
    }

    public void setActualHarvestDate(LocalDate actualHarvestDate) {
        this.actualHarvestDate = actualHarvestDate;
    }

    public String getGrowthStage() {
        return growthStage;
    }

    public void setGrowthStage(String growthStage) {
        this.growthStage = growthStage;
    }

    public String getCultivationStatus() {
        return cultivationStatus;
    }

    public void setCultivationStatus(String cultivationStatus) {
        this.cultivationStatus = cultivationStatus;
    }

    public Double getExpectedYieldKg() {
        return expectedYieldKg;
    }

    public void setExpectedYieldKg(Double expectedYieldKg) {
        this.expectedYieldKg = expectedYieldKg;
    }

    public Double getActualYieldKg() {
        return actualYieldKg;
    }

    public void setActualYieldKg(Double actualYieldKg) {
        this.actualYieldKg = actualYieldKg;
    }

    public String getFieldObservations() {
        return fieldObservations;
    }

    public void setFieldObservations(String fieldObservations) {
        this.fieldObservations = fieldObservations;
    }
}
