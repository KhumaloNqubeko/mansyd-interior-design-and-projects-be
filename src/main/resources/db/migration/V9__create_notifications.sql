CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient_user_id UUID NOT NULL,
    type VARCHAR(30) NOT NULL CHECK (type IN ('SERVICE_REQUEST', 'APPOINTMENT', 'INVOICE', 'PAYMENT', 'PROJECT', 'GENERAL')),
    title VARCHAR(160) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    action_url VARCHAR(240) NOT NULL,
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_user_id) REFERENCES users(id)
);

CREATE INDEX idx_notifications_recipient_created ON notifications (recipient_user_id, created_at DESC);
CREATE INDEX idx_notifications_recipient_unread ON notifications (recipient_user_id) WHERE read_at IS NULL;
