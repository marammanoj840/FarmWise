package com.farmwise.backend.soil;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "crop_observations")
public class CropObservationEntity extends BaseAuditEntity {

    @Column(name = "farm_id", nullable = false)
    private UUID farmId;

    @Column(name = "farm_crop_id")
    private UUID farmCropId;

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "observation_type", nullable = false, length = 40)
    private String observationType;

    @Column(name = "severity", nullable = false, length = 24)
    private String severity = "LOW";

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "symptoms", nullable = false, columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "affected_area_pct")
    private Double affectedAreaPct;

    @Column(name = "recommended_action", columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(name = "expert_verified", nullable = false)
    private boolean expertVerified = false;

    @Column(name = "observed_at", nullable = false)
    private LocalDate observedAt;

    public UUID getFarmId() {
        return farmId;
    }

    public void setFarmId(UUID farmId) {
        this.farmId = farmId;
    }

    public UUID getFarmCropId() {
        return farmCropId;
    }

    public void setFarmCropId(UUID farmCropId) {
        this.farmCropId = farmCropId;
    }

    public UUID getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(UUID farmerId) {
        this.farmerId = farmerId;
    }

    public String getObservationType() {
        return observationType;
    }

    public void setObservationType(String observationType) {
        this.observationType = observationType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public Double getAffectedAreaPct() {
        return affectedAreaPct;
    }

    public void setAffectedAreaPct(Double affectedAreaPct) {
        this.affectedAreaPct = affectedAreaPct;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public boolean isExpertVerified() {
        return expertVerified;
    }

    public void setExpertVerified(boolean expertVerified) {
        this.expertVerified = expertVerified;
    }

    public LocalDate getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(LocalDate observedAt) {
        this.observedAt = observedAt;
    }
}
