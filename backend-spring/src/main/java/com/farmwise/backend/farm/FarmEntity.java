package com.farmwise.backend.farm;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "farms")
public class FarmEntity extends BaseAuditEntity {

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "land_area", nullable = false)
    private double landArea;

    @Column(name = "area_unit", nullable = false, length = 24)
    private String areaUnit = "ACRES";

    @Column(name = "soil_type", nullable = false, length = 64)
    private String soilType;

    @Column(name = "irrigation_type", nullable = false, length = 64)
    private String irrigationType;

    @Column(name = "photo_urls_json", nullable = false, columnDefinition = "TEXT")
    private String photoUrlsJson = "[]";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public UUID getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(UUID farmerId) {
        this.farmerId = farmerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLandArea() {
        return landArea;
    }

    public void setLandArea(double landArea) {
        this.landArea = landArea;
    }

    public String getAreaUnit() {
        return areaUnit;
    }

    public void setAreaUnit(String areaUnit) {
        this.areaUnit = areaUnit;
    }

    public String getSoilType() {
        return soilType;
    }

    public void setSoilType(String soilType) {
        this.soilType = soilType;
    }

    public String getIrrigationType() {
        return irrigationType;
    }

    public void setIrrigationType(String irrigationType) {
        this.irrigationType = irrigationType;
    }

    public String getPhotoUrlsJson() {
        return photoUrlsJson;
    }

    public void setPhotoUrlsJson(String photoUrlsJson) {
        this.photoUrlsJson = photoUrlsJson;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
