package com.carpenter.business.audit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.user.AccountStatus;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {
    @Mock AuditLogRepository auditLogs;
    @Mock CurrentUser currentUser;
    private AuditLogService service;

    @BeforeEach
    void setUp() {
        service = new AuditLogService(auditLogs, currentUser);
    }

    @Test
    void auditActorIsRequired() {
        assertThatThrownBy(() -> service.record(null, AuditAction.CREATED, "Document", UUID.randomUUID(), "Created"))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    @Test
    void recordsAuditLog() {
        User actor = new User("carpenter@example.com", "hash", Role.CARPENTER, AccountStatus.ACTIVE);
        ReflectionTestUtils.setField(actor, "id", UUID.randomUUID());
        UUID entityId = UUID.randomUUID();

        service.record(actor, AuditAction.CREATED, "Document", entityId, "Created document");

        verify(auditLogs).save(org.mockito.ArgumentMatchers.any(AuditLog.class));
    }
}
