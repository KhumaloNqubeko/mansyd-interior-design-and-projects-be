CREATE TABLE documents (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    service_request_id UUID,
    project_id UUID,
    invoice_id UUID,
    uploaded_by_user_id UUID NOT NULL,
    title VARCHAR(160) NOT NULL,
    type VARCHAR(30) NOT NULL CHECK (type IN ('DESIGN', 'QUOTATION', 'CONTRACT', 'INVOICE', 'RECEIPT', 'PHOTO', 'WARRANTY', 'OTHER')),
    status VARCHAR(30) NOT NULL CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    file_name VARCHAR(180) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    file_size_bytes BIGINT CHECK (file_size_bytes IS NULL OR file_size_bytes >= 0),
    storage_url VARCHAR(500) NOT NULL,
    notes VARCHAR(1000) NOT NULL,
    customer_visible BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_documents_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_documents_service_request FOREIGN KEY (service_request_id) REFERENCES service_requests(id),
    CONSTRAINT fk_documents_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_documents_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    CONSTRAINT fk_documents_uploaded_by FOREIGN KEY (uploaded_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_documents_customer ON documents (customer_id);
CREATE INDEX idx_documents_service_request ON documents (service_request_id);
CREATE INDEX idx_documents_project ON documents (project_id);
CREATE INDEX idx_documents_invoice ON documents (invoice_id);
CREATE INDEX idx_documents_status ON documents (status);
