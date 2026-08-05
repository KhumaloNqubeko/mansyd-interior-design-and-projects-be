package com.carpenter.business.audit;

import com.carpenter.business.audit.dto.AuditLogResponse;
import com.carpenter.business.common.PageResponse;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final CurrentUser currentUser;

    public AuditLogService(AuditLogRepository auditLogRepository, CurrentUser currentUser) {
        this.auditLogRepository = auditLogRepository;
        this.currentUser = currentUser;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(User actor, AuditAction action, String entityType, UUID entityId, String summary) {
        if (actor == null) throw new UnauthorisedOperationException("Audit actor is required.");
        auditLogRepository.save(new AuditLog(actor, action, clean(entityType), entityId, clean(summary)));
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> all(Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(auditLogRepository.findAll(pageable).map(AuditLogResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> byEntity(String entityType, UUID entityId, Authentication authentication,
                                                  Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable)
                .map(AuditLogResponse::from));
    }

    private String clean(String value) { return value == null ? "" : value.trim(); }
}
