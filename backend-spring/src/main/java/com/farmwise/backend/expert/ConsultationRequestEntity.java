package com.farmwise.backend.expert;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "consultation_requests")
public class ConsultationRequestEntity extends BaseAuditEntity {

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "expert_id", nullable = false)
    private UUID expertId;

    @Column(name = "farm_id")
    private UUID farmId;

    @Column(name = "topic", nullable = false, length = 180)
    private String topic;

    @Column(name = "crop_name", length = 120)
    private String cropName;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "urgency", nullable = false, length = 24)
    private String urgency = "NORMAL";

    @Column(name = "status", nullable = false, length = 24)
    private String status = "PENDING";

    @Column(name = "expert_response", columnDefinition = "TEXT")
    private String expertResponse;

    @Column(name = "responded_at")
    private OffsetDateTime respondedAt;

    public UUID getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(UUID farmerId) {
        this.farmerId = farmerId;
    }

    public UUID getExpertId() {
        return expertId;
    }

    public void setExpertId(UUID expertId) {
        this.expertId = expertId;
    }

    public UUID getFarmId() {
        return farmId;
    }

    public void setFarmId(UUID farmId) {
        this.farmId = farmId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExpertResponse() {
        return expertResponse;
    }

    public void setExpertResponse(String expertResponse) {
        this.expertResponse = expertResponse;
    }

    public OffsetDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(OffsetDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}
