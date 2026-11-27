package com.farmwise.backend.inventory;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "inventory_items")
public class InventoryItemEntity extends BaseAuditEntity {

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "farm_id")
    private UUID farmId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "category", nullable = false, length = 40)
    private String category;

    @Column(name = "sku", length = 64)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private double quantity;

    @Column(name = "unit", nullable = false, length = 32)
    private String unit;

    @Column(name = "reorder_threshold", nullable = false)
    private double reorderThreshold;

    @Column(name = "unit_cost_inr", nullable = false, precision = 14, scale = 2)
    private BigDecimal unitCostInr = BigDecimal.ZERO;

    @Column(name = "supplier_name", length = 150)
    private String supplierName;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
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

    public double getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(double reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public BigDecimal getUnitCostInr() {
        return unitCostInr;
    }

    public void setUnitCostInr(BigDecimal unitCostInr) {
        this.unitCostInr = unitCostInr;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
