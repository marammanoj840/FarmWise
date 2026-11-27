package com.farmwise.backend.marketplace;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "produce_listings")
public class ProduceListingEntity extends BaseAuditEntity {

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "farm_id")
    private UUID farmId;

    @Column(name = "crop_name", nullable = false, length = 120)
    private String cropName;

    @Column(name = "variety", length = 120)
    private String variety;

    @Column(name = "quantity", nullable = false)
    private double quantity;

    @Column(name = "unit", nullable = false, length = 32)
    private String unit = "QUINTAL";

    @Column(name = "expected_price_inr", nullable = false, precision = 14, scale = 2)
    private BigDecimal expectedPriceInr;

    @Column(name = "location", nullable = false, length = 180)
    private String location;

    @Column(name = "quality_grade", nullable = false, length = 64)
    private String qualityGrade;

    @Column(name = "quality_notes", columnDefinition = "TEXT")
    private String qualityNotes;

    @Column(name = "available_from")
    private LocalDate availableFrom;

    @Column(name = "harvest_date")
    private LocalDate harvestDate;

    @Column(name = "listing_status", nullable = false, length = 32)
    private String listingStatus = "ACTIVE";

    @Column(name = "moderation_status", nullable = false, length = 24)
    private String moderationStatus = "APPROVED";

    public UUID getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(UUID farmerId) {
        this.farmerId = farmerId;
    }

    public UUID getFarmId() {
        return farmId;
    }

    public void setFarmId(UUID farmId) {
        this.farmId = farmId;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getExpectedPriceInr() {
        return expectedPriceInr;
    }

    public void setExpectedPriceInr(BigDecimal expectedPriceInr) {
        this.expectedPriceInr = expectedPriceInr;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public String getQualityNotes() {
        return qualityNotes;
    }

    public void setQualityNotes(String qualityNotes) {
        this.qualityNotes = qualityNotes;
    }

    public LocalDate getAvailableFrom() {
        return availableFrom;
    }

    public void setAvailableFrom(LocalDate availableFrom) {
        this.availableFrom = availableFrom;
    }

    public LocalDate getHarvestDate() {
        return harvestDate;
    }

    public void setHarvestDate(LocalDate harvestDate) {
        this.harvestDate = harvestDate;
    }

    public String getListingStatus() {
        return listingStatus;
    }

    public void setListingStatus(String listingStatus) {
        this.listingStatus = listingStatus;
    }

    public String getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(String moderationStatus) {
        this.moderationStatus = moderationStatus;
    }
}
