package com.farmwise.backend.assistant;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "assistant_conversations")
public class AssistantConversationEntity extends BaseAuditEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "farm_id")
    private UUID farmId;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "provider_name", nullable = false, length = 64)
    private String providerName;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getFarmId() {
        return farmId;
    }

    public void setFarmId(UUID farmId) {
        this.farmId = farmId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }
}
