package com.farmwise.backend.admin;

import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public void record(
            UUID actorUserId,
            String actorEmail,
            String action,
            String entityType,
            String entityId,
            Map<String, Object> details,
            String ipAddress) {
        AuditLogEntity entry = new AuditLogEntity();
        entry.setActorUserId(actorUserId);
        entry.setActorEmail(actorEmail);
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setIpAddress(ipAddress);
        try {
            entry.setDetailsJson(details == null ? "{}" : objectMapper.writeValueAsString(details));
        } catch (Exception ex) {
            entry.setDetailsJson("{}");
        }
        auditLogRepository.save(entry);
    }
}
