package com.farmwise.backend.soil;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "crop_knowledge_base")
public class CropKnowledgeBaseEntity extends BaseAuditEntity {

    @Column(name = "crop_name", nullable = false, length = 100)
    private String cropName;

    @Column(name = "category", nullable = false, length = 64)
    private String category;

    @Column(name = "suitable_soil_types", nullable = false, length = 255)
    private String suitableSoilTypes;

    @Column(name = "ideal_ph_range", nullable = false, length = 64)
    private String idealPhRange;

    @Column(name = "sowing_season", nullable = false, length = 120)
    private String sowingSeason;

    @Column(name = "irrigation_schedule", nullable = false, columnDefinition = "TEXT")
    private String irrigationSchedule;

    @Column(name = "nutrient_guidance", nullable = false, columnDefinition = "TEXT")
    private String nutrientGuidance;

    @Column(name = "common_pests_diseases", nullable = false, columnDefinition = "TEXT")
    private String commonPestsDiseases;

    @Column(name = "advisory_disclaimer", nullable = false, columnDefinition = "TEXT")
    private String advisoryDisclaimer;

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSuitableSoilTypes() {
        return suitableSoilTypes;
    }

    public void setSuitableSoilTypes(String suitableSoilTypes) {
        this.suitableSoilTypes = suitableSoilTypes;
    }

    public String getIdealPhRange() {
        return idealPhRange;
    }

    public void setIdealPhRange(String idealPhRange) {
        this.idealPhRange = idealPhRange;
    }

    public String getSowingSeason() {
        return sowingSeason;
    }

    public void setSowingSeason(String sowingSeason) {
        this.sowingSeason = sowingSeason;
    }

    public String getIrrigationSchedule() {
        return irrigationSchedule;
    }

    public void setIrrigationSchedule(String irrigationSchedule) {
        this.irrigationSchedule = irrigationSchedule;
    }

    public String getNutrientGuidance() {
        return nutrientGuidance;
    }

    public void setNutrientGuidance(String nutrientGuidance) {
        this.nutrientGuidance = nutrientGuidance;
    }

    public String getCommonPestsDiseases() {
        return commonPestsDiseases;
    }

    public void setCommonPestsDiseases(String commonPestsDiseases) {
        this.commonPestsDiseases = commonPestsDiseases;
    }

    public String getAdvisoryDisclaimer() {
        return advisoryDisclaimer;
    }

    public void setAdvisoryDisclaimer(String advisoryDisclaimer) {
        this.advisoryDisclaimer = advisoryDisclaimer;
    }
}
