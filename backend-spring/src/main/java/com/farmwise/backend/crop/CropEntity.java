package com.farmwise.backend.crop;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "crops")
public class CropEntity extends BaseAuditEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "scientific_name", length = 150)
    private String scientificName;

    @Column(name = "category", nullable = false, length = 64)
    private String category;

    @Column(name = "typical_duration_days")
    private Integer typicalDurationDays;

    @Column(name = "water_requirement", length = 64)
    private String waterRequirement;

    @Column(name = "optimal_ph_min")
    private Double optimalPhMin;

    @Column(name = "optimal_ph_max")
    private Double optimalPhMax;

    @Column(name = "season_suitability", length = 120)
    private String seasonSuitability;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getTypicalDurationDays() {
        return typicalDurationDays;
    }

    public void setTypicalDurationDays(Integer typicalDurationDays) {
        this.typicalDurationDays = typicalDurationDays;
    }

    public String getWaterRequirement() {
        return waterRequirement;
    }

    public void setWaterRequirement(String waterRequirement) {
        this.waterRequirement = waterRequirement;
    }

    public Double getOptimalPhMin() {
        return optimalPhMin;
    }

    public void setOptimalPhMin(Double optimalPhMin) {
        this.optimalPhMin = optimalPhMin;
    }

    public Double getOptimalPhMax() {
        return optimalPhMax;
    }

    public void setOptimalPhMax(Double optimalPhMax) {
        this.optimalPhMax = optimalPhMax;
    }

    public String getSeasonSuitability() {
        return seasonSuitability;
    }

    public void setSeasonSuitability(String seasonSuitability) {
        this.seasonSuitability = seasonSuitability;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
