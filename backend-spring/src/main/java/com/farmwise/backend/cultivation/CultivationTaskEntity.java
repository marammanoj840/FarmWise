package com.farmwise.backend.cultivation;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "cultivation_tasks")
public class CultivationTaskEntity extends BaseAuditEntity {

    @Column(name = "farm_id", nullable = false)
    private UUID farmId;

    @Column(name = "farm_crop_id")
    private UUID farmCropId;

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "stage", nullable = false, length = 40)
    private String stage;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "reminder_at")
    private OffsetDateTime reminderAt;

    @Column(name = "priority", nullable = false, length = 24)
    private String priority = "MEDIUM";

    @Column(name = "status", nullable = false, length = 32)
    private String status = "PENDING";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    public UUID getFarmId() {
        return farmId;
    }

    public void setFarmId(UUID farmId) {
        this.farmId = farmId;
    }

    public UUID getFarmCropId() {
        return farmCropId;
    }

    public void setFarmCropId(UUID farmCropId) {
        this.farmCropId = farmCropId;
    }

    public UUID getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(UUID farmerId) {
        this.farmerId = farmerId;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public OffsetDateTime getReminderAt() {
        return reminderAt;
    }

    public void setReminderAt(OffsetDateTime reminderAt) {
        this.reminderAt = reminderAt;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
