package com.farmwise.backend.finance;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "income")
public class IncomeEntity extends BaseAuditEntity {

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "farm_id")
    private UUID farmId;

    @Column(name = "farm_crop_id")
    private UUID farmCropId;

    @Column(name = "category", nullable = false, length = 48)
    private String category;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "amount_inr", nullable = false, precision = 14, scale = 2)
    private BigDecimal amountInr;

    @Column(name = "currency", nullable = false, length = 8)
    private String currency = "INR";

    @Column(name = "income_date", nullable = false)
    private LocalDate incomeDate;

    @Column(name = "buyer_name", length = 140)
    private String buyerName;

    @Column(name = "quantity_sold")
    private Double quantitySold;

    @Column(name = "unit", length = 32)
    private String unit;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

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

    public UUID getFarmCropId() {
        return farmCropId;
    }

    public void setFarmCropId(UUID farmCropId) {
        this.farmCropId = farmCropId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmountInr() {
        return amountInr;
    }

    public void setAmountInr(BigDecimal amountInr) {
        this.amountInr = amountInr;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getIncomeDate() {
        return incomeDate;
    }

    public void setIncomeDate(LocalDate incomeDate) {
        this.incomeDate = incomeDate;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public Double getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(Double quantitySold) {
        this.quantitySold = quantitySold;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
