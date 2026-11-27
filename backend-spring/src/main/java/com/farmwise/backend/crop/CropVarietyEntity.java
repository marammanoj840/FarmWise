package com.farmwise.backend.crop;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "crop_varieties")
public class CropVarietyEntity extends BaseAuditEntity {

    @Column(name = "crop_id", nullable = false)
    private UUID cropId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "maturity_days")
    private Integer maturityDays;

    @Column(name = "expected_yield_per_acre_kg")
    private Double expectedYieldPerAcreKg;

    @Column(name = "climate_notes", columnDefinition = "TEXT")
    private String climateNotes;

    @Column(name = "disease_resistance", columnDefinition = "TEXT")
    private String diseaseResistance;

    public UUID getCropId() {
        return cropId;
    }

    public void setCropId(UUID cropId) {
        this.cropId = cropId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaturityDays() {
        return maturityDays;
    }

    public void setMaturityDays(Integer maturityDays) {
        this.maturityDays = maturityDays;
    }

    public Double getExpectedYieldPerAcreKg() {
        return expectedYieldPerAcreKg;
    }

    public void setExpectedYieldPerAcreKg(Double expectedYieldPerAcreKg) {
        this.expectedYieldPerAcreKg = expectedYieldPerAcreKg;
    }

    public String getClimateNotes() {
        return climateNotes;
    }

    public void setClimateNotes(String climateNotes) {
        this.climateNotes = climateNotes;
    }

    public String getDiseaseResistance() {
        return diseaseResistance;
    }

    public void setDiseaseResistance(String diseaseResistance) {
        this.diseaseResistance = diseaseResistance;
    }
}
