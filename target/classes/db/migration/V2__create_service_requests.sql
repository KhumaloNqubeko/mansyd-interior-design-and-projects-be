CREATE TABLE service_requests (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    title VARCHAR(140) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    preferred_contact_method VARCHAR(30) NOT NULL,
    site_address VARCHAR(300) NOT NULL,
    status VARCHAR(40) NOT NULL CHECK (status IN (
        'SUBMITTED',
        'UNDER_REVIEW',
        'SITE_VISIT_REQUIRED',
        'QUOTATION_IN_PROGRESS',
        'QUOTED',
        'CANCELLED',
        'CLOSED'
    )),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_service_requests_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE INDEX idx_service_requests_customer ON service_requests (customer_id);
CREATE INDEX idx_service_requests_status ON service_requests (status);
CREATE INDEX idx_service_requests_created_at ON service_requests (created_at);
