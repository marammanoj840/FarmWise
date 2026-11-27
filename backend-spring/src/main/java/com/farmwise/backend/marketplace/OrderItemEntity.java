package com.farmwise.backend.marketplace;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItemEntity extends BaseAuditEntity {

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "supplier_id", nullable = false)
    private UUID supplierId;

    @Column(name = "product_name_snapshot", nullable = false, length = 180)
    private String productNameSnapshot;

    @Column(name = "unit_snapshot", nullable = false, length = 32)
    private String unitSnapshot;

    @Column(name = "unit_price_inr", nullable = false, precision = 14, scale = 2)
    private BigDecimal unitPriceInr;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "line_total_inr", nullable = false, precision = 14, scale = 2)
    private BigDecimal lineTotalInr;

    @Column(name = "fulfillment_status", nullable = false, length = 32)
    private String fulfillmentStatus = "PENDING";

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
    }

    public String getProductNameSnapshot() {
        return productNameSnapshot;
    }

    public void setProductNameSnapshot(String productNameSnapshot) {
        this.productNameSnapshot = productNameSnapshot;
    }

    public String getUnitSnapshot() {
        return unitSnapshot;
    }

    public void setUnitSnapshot(String unitSnapshot) {
        this.unitSnapshot = unitSnapshot;
    }

    public BigDecimal getUnitPriceInr() {
        return unitPriceInr;
    }

    public void setUnitPriceInr(BigDecimal unitPriceInr) {
        this.unitPriceInr = unitPriceInr;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getLineTotalInr() {
        return lineTotalInr;
    }

    public void setLineTotalInr(BigDecimal lineTotalInr) {
        this.lineTotalInr = lineTotalInr;
    }

    public String getFulfillmentStatus() {
        return fulfillmentStatus;
    }

    public void setFulfillmentStatus(String fulfillmentStatus) {
        this.fulfillmentStatus = fulfillmentStatus;
    }
}
