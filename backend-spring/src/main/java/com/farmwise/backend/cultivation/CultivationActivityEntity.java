package com.farmwise.backend.cultivation;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cultivation_activities")
public class CultivationActivityEntity extends BaseAuditEntity {

    @Column(name = "farm_id", nullable = false)
    private UUID farmId;

    @Column(name = "farm_crop_id")
    private UUID farmCropId;

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "stage", nullable = false, length = 40)
    private String stage;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "input_used", length = 160)
    private String inputUsed;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "unit", length = 32)
    private String unit;

    @Column(name = "cost_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal costAmount = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

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

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(LocalDate activityDate) {
        this.activityDate = activityDate;
    }

    public String getInputUsed() {
        return inputUsed;
    }

    public void setInputUsed(String inputUsed) {
        this.inputUsed = inputUsed;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getCostAmount() {
        return costAmount;
    }

    public void setCostAmount(BigDecimal costAmount) {
        this.costAmount = costAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
