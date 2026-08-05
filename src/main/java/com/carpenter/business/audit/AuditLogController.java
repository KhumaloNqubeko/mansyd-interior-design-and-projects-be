package com.carpenter.business.audit;

import com.carpenter.business.audit.dto.AuditLogResponse;
import com.carpenter.business.common.PageResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    PageResponse<AuditLogResponse> all(@RequestParam(required = false) String entityType,
                                       @RequestParam(required = false) UUID entityId,
                                       Authentication authentication,
                                       @PageableDefault(size = 50) Pageable pageable) {
        if (entityType != null && entityId != null) {
            return auditLogService.byEntity(entityType, entityId, authentication, pageable);
        }
        return auditLogService.all(authentication, pageable);
    }
}
