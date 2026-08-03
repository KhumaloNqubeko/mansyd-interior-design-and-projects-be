CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    invoice_number VARCHAR(40) NOT NULL,
    order_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL CHECK (status IN ('DRAFT', 'ISSUED', 'PARTIALLY_PAID', 'PAID', 'CANCELLED')),
    issue_date DATE,
    due_date DATE NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL CHECK (total_amount >= 0),
    paid_amount NUMERIC(12, 2) NOT NULL CHECK (paid_amount >= 0),
    balance_due NUMERIC(12, 2) NOT NULL CHECK (balance_due >= 0),
    notes VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_invoices_number UNIQUE (invoice_number),
    CONSTRAINT uk_invoices_order UNIQUE (order_id),
    CONSTRAINT fk_invoices_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_invoices_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE INDEX idx_invoices_customer ON invoices (customer_id);
CREATE INDEX idx_invoices_status ON invoices (status);
CREATE INDEX idx_invoices_due_date ON invoices (due_date);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    payment_date DATE NOT NULL,
    proof_reference VARCHAR(180) NOT NULL,
    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING_REVIEW', 'APPROVED', 'REJECTED')),
    notes VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    CONSTRAINT fk_payments_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE INDEX idx_payments_invoice ON payments (invoice_id);
CREATE INDEX idx_payments_customer ON payments (customer_id);
CREATE INDEX idx_payments_status ON payments (status);
