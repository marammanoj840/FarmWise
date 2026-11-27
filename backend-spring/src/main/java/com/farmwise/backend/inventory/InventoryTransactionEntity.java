package com.farmwise.backend.inventory;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "inventory_transactions")
public class InventoryTransactionEntity extends BaseAuditEntity {

    @Column(name = "inventory_item_id", nullable = false)
    private UUID inventoryItemId;

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "transaction_type", nullable = false, length = 24)
    private String transactionType;

    @Column(name = "quantity_delta", nullable = false)
    private double quantityDelta;

    @Column(name = "unit_cost_inr", nullable = false, precision = 14, scale = 2)
    private BigDecimal unitCostInr = BigDecimal.ZERO;

    @Column(name = "reference_note", length = 255)
    private String referenceNote;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    public UUID getInventoryItemId() {
        return inventoryItemId;
    }

    public void setInventoryItemId(UUID inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    public UUID getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(UUID farmerId) {
        this.farmerId = farmerId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getQuantityDelta() {
        return quantityDelta;
    }

    public void setQuantityDelta(double quantityDelta) {
        this.quantityDelta = quantityDelta;
    }

    public BigDecimal getUnitCostInr() {
        return unitCostInr;
    }

    public void setUnitCostInr(BigDecimal unitCostInr) {
        this.unitCostInr = unitCostInr;
    }

    public String getReferenceNote() {
        return referenceNote;
    }

    public void setReferenceNote(String referenceNote) {
        this.referenceNote = referenceNote;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }
}
