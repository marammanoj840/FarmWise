package com.farmwise.backend.weather;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "weather_records")
public class WeatherRecordEntity extends BaseAuditEntity {

    @Column(name = "farm_id")
    private UUID farmId;

    @Column(name = "location_name", nullable = false, length = 150)
    private String locationName;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "temperature_c")
    private Double temperatureC;

    @Column(name = "humidity_pct")
    private Double humidityPct;

    @Column(name = "rainfall_mm")
    private Double rainfallMm;

    @Column(name = "wind_speed_kmh")
    private Double windSpeedKmh;

    @Column(name = "condition_summary", length = 140)
    private String conditionSummary;

    @Column(name = "alert_level", nullable = false, length = 24)
    private String alertLevel = "NONE";

    @Column(name = "agronomic_advice", columnDefinition = "TEXT")
    private String agronomicAdvice;

    @Column(name = "provider_name", nullable = false, length = 64)
    private String providerName;

    @Column(name = "is_live_data", nullable = false)
    private boolean liveData = false;

    @Column(name = "observed_at", nullable = false)
    private OffsetDateTime observedAt;

    public UUID getFarmId() {
        return farmId;
    }

    public void setFarmId(UUID farmId) {
        this.farmId = farmId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(Double temperatureC) {
        this.temperatureC = temperatureC;
    }

    public Double getHumidityPct() {
        return humidityPct;
    }

    public void setHumidityPct(Double humidityPct) {
        this.humidityPct = humidityPct;
    }

    public Double getRainfallMm() {
        return rainfallMm;
    }

    public void setRainfallMm(Double rainfallMm) {
        this.rainfallMm = rainfallMm;
    }

    public Double getWindSpeedKmh() {
        return windSpeedKmh;
    }

    public void setWindSpeedKmh(Double windSpeedKmh) {
        this.windSpeedKmh = windSpeedKmh;
    }

    public String getConditionSummary() {
        return conditionSummary;
    }

    public void setConditionSummary(String conditionSummary) {
        this.conditionSummary = conditionSummary;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getAgronomicAdvice() {
        return agronomicAdvice;
    }

    public void setAgronomicAdvice(String agronomicAdvice) {
        this.agronomicAdvice = agronomicAdvice;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public boolean isLiveData() {
        return liveData;
    }

    public void setLiveData(boolean liveData) {
        this.liveData = liveData;
    }

    public OffsetDateTime getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(OffsetDateTime observedAt) {
        this.observedAt = observedAt;
    }
}
