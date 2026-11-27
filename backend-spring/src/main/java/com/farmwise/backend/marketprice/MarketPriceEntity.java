package com.farmwise.backend.marketprice;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "market_prices")
public class MarketPriceEntity extends BaseAuditEntity {

    @Column(name = "commodity", nullable = false, length = 120)
    private String commodity;

    @Column(name = "variety", length = 120)
    private String variety;

    @Column(name = "market_name", nullable = false, length = 150)
    private String marketName;

    @Column(name = "district", nullable = false, length = 120)
    private String district;

    @Column(name = "state", nullable = false, length = 120)
    private String state;

    @Column(name = "min_price_inr", nullable = false, precision = 14, scale = 2)
    private BigDecimal minPriceInr;

    @Column(name = "max_price_inr", nullable = false, precision = 14, scale = 2)
    private BigDecimal maxPriceInr;

    @Column(name = "modal_price_inr", nullable = false, precision = 14, scale = 2)
    private BigDecimal modalPriceInr;

    @Column(name = "unit", nullable = false, length = 32)
    private String unit = "QUINTAL";

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(name = "data_source", nullable = false, length = 64)
    private String dataSource;

    @Column(name = "is_sample_data", nullable = false)
    private boolean sampleData = true;

    @Column(name = "fetched_at", nullable = false)
    private OffsetDateTime fetchedAt;

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public BigDecimal getMinPriceInr() {
        return minPriceInr;
    }

    public void setMinPriceInr(BigDecimal minPriceInr) {
        this.minPriceInr = minPriceInr;
    }

    public BigDecimal getMaxPriceInr() {
        return maxPriceInr;
    }

    public void setMaxPriceInr(BigDecimal maxPriceInr) {
        this.maxPriceInr = maxPriceInr;
    }

    public BigDecimal getModalPriceInr() {
        return modalPriceInr;
    }

    public void setModalPriceInr(BigDecimal modalPriceInr) {
        this.modalPriceInr = modalPriceInr;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDate getPriceDate() {
        return priceDate;
    }

    public void setPriceDate(LocalDate priceDate) {
        this.priceDate = priceDate;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isSampleData() {
        return sampleData;
    }

    public void setSampleData(boolean sampleData) {
        this.sampleData = sampleData;
    }

    public OffsetDateTime getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(OffsetDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
}
