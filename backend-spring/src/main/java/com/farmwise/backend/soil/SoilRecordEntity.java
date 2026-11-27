package com.farmwise.backend.soil;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "soil_records")
public class SoilRecordEntity extends BaseAuditEntity {

    @Column(name = "farm_id", nullable = false)
    private UUID farmId;

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "soil_type", nullable = false, length = 64)
    private String soilType;

    @Column(name = "ph", nullable = false)
    private double ph;

    @Column(name = "nitrogen")
    private Double nitrogen;

    @Column(name = "phosphorus")
    private Double phosphorus;

    @Column(name = "potassium")
    private Double potassium;

    @Column(name = "organic_carbon_pct")
    private Double organicCarbonPct;

    @Column(name = "moisture_pct")
    private Double moisturePct;

    @Column(name = "fertility_rating", length = 40)
    private String fertilityRating;

    @Column(name = "lab_name", length = 140)
    private String labName;

    @Column(name = "test_date", nullable = false)
    private LocalDate testDate;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

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

    public String getSoilType() {
        return soilType;
    }

    public void setSoilType(String soilType) {
        this.soilType = soilType;
    }

    public double getPh() {
        return ph;
    }

    public void setPh(double ph) {
        this.ph = ph;
    }

    public Double getNitrogen() {
        return nitrogen;
    }

    public void setNitrogen(Double nitrogen) {
        this.nitrogen = nitrogen;
    }

    public Double getPhosphorus() {
        return phosphorus;
    }

    public void setPhosphorus(Double phosphorus) {
        this.phosphorus = phosphorus;
    }

    public Double getPotassium() {
        return potassium;
    }

    public void setPotassium(Double potassium) {
        this.potassium = potassium;
    }

    public Double getOrganicCarbonPct() {
        return organicCarbonPct;
    }

    public void setOrganicCarbonPct(Double organicCarbonPct) {
        this.organicCarbonPct = organicCarbonPct;
    }

    public Double getMoisturePct() {
        return moisturePct;
    }

    public void setMoisturePct(Double moisturePct) {
        this.moisturePct = moisturePct;
    }

    public String getFertilityRating() {
        return fertilityRating;
    }

    public void setFertilityRating(String fertilityRating) {
        this.fertilityRating = fertilityRating;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public LocalDate getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDate testDate) {
        this.testDate = testDate;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }
}
