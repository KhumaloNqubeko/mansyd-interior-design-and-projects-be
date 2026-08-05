CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID NOT NULL,
    action VARCHAR(30) NOT NULL CHECK (action IN ('CREATED', 'UPDATED', 'STATUS_CHANGED', 'APPROVED', 'REJECTED', 'ARCHIVED', 'ISSUED', 'SUBMITTED')),
    entity_type VARCHAR(80) NOT NULL,
    entity_id UUID NOT NULL,
    summary VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_audit_logs_actor FOREIGN KEY (actor_user_id) REFERENCES users(id)
);

CREATE INDEX idx_audit_logs_created ON audit_logs (created_at DESC);
CREATE INDEX idx_audit_logs_actor ON audit_logs (actor_user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
