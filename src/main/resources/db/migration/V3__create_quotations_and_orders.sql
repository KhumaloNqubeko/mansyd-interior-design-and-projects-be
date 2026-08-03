CREATE TABLE quotations (
    id UUID PRIMARY KEY,
    quotation_number VARCHAR(40) NOT NULL,
    service_request_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL CHECK (status IN ('DRAFT', 'PENDING_CUSTOMER', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'CANCELLED')),
    expiry_date DATE NOT NULL,
    notes VARCHAR(1000) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0),
    discount_total NUMERIC(12, 2) NOT NULL CHECK (discount_total >= 0),
    tax_total NUMERIC(12, 2) NOT NULL CHECK (tax_total >= 0),
    total NUMERIC(12, 2) NOT NULL CHECK (total >= 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_quotations_number UNIQUE (quotation_number),
    CONSTRAINT uk_quotations_service_request UNIQUE (service_request_id),
    CONSTRAINT fk_quotations_service_request FOREIGN KEY (service_request_id) REFERENCES service_requests(id),
    CONSTRAINT fk_quotations_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE INDEX idx_quotations_customer ON quotations (customer_id);
CREATE INDEX idx_quotations_status ON quotations (status);
CREATE INDEX idx_quotations_expiry_date ON quotations (expiry_date);

CREATE TABLE quotation_items (
    id UUID PRIMARY KEY,
    quotation_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('MATERIAL', 'LABOUR', 'DELIVERY', 'OTHER')),
    description VARCHAR(180) NOT NULL,
    quantity NUMERIC(12, 3) NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    discount_amount NUMERIC(12, 2) NOT NULL CHECK (discount_amount >= 0),
    tax_rate NUMERIC(5, 2) NOT NULL CHECK (tax_rate >= 0),
    line_total NUMERIC(12, 2) NOT NULL CHECK (line_total >= 0),
    CONSTRAINT fk_quotation_items_quotation FOREIGN KEY (quotation_id) REFERENCES quotations(id) ON DELETE CASCADE
);

CREATE INDEX idx_quotation_items_quotation ON quotation_items (quotation_id);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    order_number VARCHAR(40) NOT NULL,
    quotation_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    accepted_total NUMERIC(12, 2) NOT NULL CHECK (accepted_total >= 0),
    status VARCHAR(30) NOT NULL CHECK (status IN ('CREATED', 'CONFIRMED', 'IN_PROGRESS', 'ON_HOLD', 'READY_FOR_DELIVERY', 'DELIVERED', 'INSTALLED', 'COMPLETED', 'CANCELLED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_orders_number UNIQUE (order_number),
    CONSTRAINT uk_orders_quotation UNIQUE (quotation_id),
    CONSTRAINT fk_orders_quotation FOREIGN KEY (quotation_id) REFERENCES quotations(id),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE INDEX idx_orders_customer ON orders (customer_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_created_at ON orders (created_at);
