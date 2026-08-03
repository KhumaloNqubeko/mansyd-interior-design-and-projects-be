CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    service_request_id UUID,
    project_id UUID,
    created_by_user_id UUID NOT NULL,
    title VARCHAR(160) NOT NULL,
    type VARCHAR(30) NOT NULL CHECK (type IN ('SITE_VISIT', 'MEASUREMENT', 'DESIGN_REVIEW', 'INSTALLATION', 'FOLLOW_UP', 'OTHER')),
    status VARCHAR(30) NOT NULL CHECK (status IN ('SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    scheduled_start TIMESTAMP NOT NULL,
    scheduled_end TIMESTAMP NOT NULL,
    location VARCHAR(300) NOT NULL,
    notes VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_appointments_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_appointments_service_request FOREIGN KEY (service_request_id) REFERENCES service_requests(id),
    CONSTRAINT fk_appointments_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_appointments_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT ck_appointments_time_window CHECK (scheduled_end > scheduled_start)
);

CREATE INDEX idx_appointments_customer ON appointments (customer_id);
CREATE INDEX idx_appointments_service_request ON appointments (service_request_id);
CREATE INDEX idx_appointments_project ON appointments (project_id);
CREATE INDEX idx_appointments_status ON appointments (status);
CREATE INDEX idx_appointments_scheduled_start ON appointments (scheduled_start);
