package com.farmwise.backend.expert;

import com.farmwise.backend.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
public class AppointmentEntity extends BaseAuditEntity {

    @Column(name = "consultation_request_id")
    private UUID consultationRequestId;

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "expert_id", nullable = false)
    private UUID expertId;

    @Column(name = "scheduled_start", nullable = false)
    private OffsetDateTime scheduledStart;

    @Column(name = "scheduled_end", nullable = false)
    private OffsetDateTime scheduledEnd;

    @Column(name = "meeting_mode", nullable = false, length = 32)
    private String meetingMode = "VIDEO";

    @Column(name = "meeting_notes", columnDefinition = "TEXT")
    private String meetingNotes;

    @Column(name = "status", nullable = false, length = 24)
    private String status = "SCHEDULED";

    public UUID getConsultationRequestId() {
        return consultationRequestId;
    }

    public void setConsultationRequestId(UUID consultationRequestId) {
        this.consultationRequestId = consultationRequestId;
    }

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

    public OffsetDateTime getScheduledStart() {
        return scheduledStart;
    }

    public void setScheduledStart(OffsetDateTime scheduledStart) {
        this.scheduledStart = scheduledStart;
    }

    public OffsetDateTime getScheduledEnd() {
        return scheduledEnd;
    }

    public void setScheduledEnd(OffsetDateTime scheduledEnd) {
        this.scheduledEnd = scheduledEnd;
    }

    public String getMeetingMode() {
        return meetingMode;
    }

    public void setMeetingMode(String meetingMode) {
        this.meetingMode = meetingMode;
    }

    public String getMeetingNotes() {
        return meetingNotes;
    }

    public void setMeetingNotes(String meetingNotes) {
        this.meetingNotes = meetingNotes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
