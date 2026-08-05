package com.carpenter.business.audit.dto;

import com.carpenter.business.audit.AuditAction;
import com.carpenter.business.audit.AuditLog;
import com.carpenter.business.user.Role;
import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(UUID id, UUID actorId, String actorEmail, Role actorRole, AuditAction action,
                               String entityType, UUID entityId, String summary, Instant createdAt) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getActor().getId(), log.getActor().getEmail(),
                log.getActor().getRole(), log.getAction(), log.getEntityType(), log.getEntityId(),
                log.getSummary(), log.getCreatedAt());
    }
}
