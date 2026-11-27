package com.farmwise.backend.marketplace;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "produce_inquiries")
public class ProduceInquiryEntity extends BaseAuditEntity {

    @Column(name = "produce_listing_id", nullable = false)
    private UUID produceListingId;

    @Column(name = "buyer_user_id", nullable = false)
    private UUID buyerUserId;

    @Column(name = "offered_price_inr", precision = 14, scale = 2)
    private BigDecimal offeredPriceInr;

    @Column(name = "requested_quantity", nullable = false)
    private double requestedQuantity;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "status", nullable = false, length = 24)
    private String status = "OPEN";

    @Column(name = "farmer_reply", columnDefinition = "TEXT")
    private String farmerReply;

    public UUID getProduceListingId() {
        return produceListingId;
    }

    public void setProduceListingId(UUID produceListingId) {
        this.produceListingId = produceListingId;
    }

    public UUID getBuyerUserId() {
        return buyerUserId;
    }

    public void setBuyerUserId(UUID buyerUserId) {
        this.buyerUserId = buyerUserId;
    }

    public BigDecimal getOfferedPriceInr() {
        return offeredPriceInr;
    }

    public void setOfferedPriceInr(BigDecimal offeredPriceInr) {
        this.offeredPriceInr = offeredPriceInr;
    }

    public double getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(double requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFarmerReply() {
        return farmerReply;
    }

    public void setFarmerReply(String farmerReply) {
        this.farmerReply = farmerReply;
    }
}
