CREATE TABLE suppliers (
    id UUID PRIMARY KEY,
    name VARCHAR(140) NOT NULL,
    contact_name VARCHAR(160),
    email VARCHAR(254),
    phone_number VARCHAR(30),
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_suppliers_name UNIQUE (name)
);

CREATE TABLE materials (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(160) NOT NULL,
    unit_of_measure VARCHAR(30) NOT NULL CHECK (unit_of_measure IN ('EACH', 'METRE', 'SQUARE_METRE', 'LITRE', 'KILOGRAM', 'PACK', 'SHEET')),
    unit_cost NUMERIC(12, 2) NOT NULL CHECK (unit_cost >= 0),
    stock_quantity NUMERIC(12, 3) NOT NULL CHECK (stock_quantity >= 0),
    reorder_level NUMERIC(12, 3) NOT NULL CHECK (reorder_level >= 0),
    supplier_id UUID,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_materials_code UNIQUE (code),
    CONSTRAINT fk_materials_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

CREATE INDEX idx_materials_supplier ON materials (supplier_id);
CREATE INDEX idx_materials_active ON materials (active);

CREATE TABLE stock_transactions (
    id UUID PRIMARY KEY,
    material_id UUID NOT NULL,
    project_id UUID,
    created_by_user_id UUID NOT NULL,
    type VARCHAR(30) NOT NULL CHECK (type IN ('STOCK_IN', 'PROJECT_ALLOCATION', 'PROJECT_RETURN', 'ADJUSTMENT_IN', 'ADJUSTMENT_OUT', 'DAMAGED')),
    quantity NUMERIC(12, 3) NOT NULL CHECK (quantity > 0),
    notes VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_stock_transactions_material FOREIGN KEY (material_id) REFERENCES materials(id),
    CONSTRAINT fk_stock_transactions_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_stock_transactions_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_stock_transactions_material ON stock_transactions (material_id);
CREATE INDEX idx_stock_transactions_project ON stock_transactions (project_id);
CREATE INDEX idx_stock_transactions_type ON stock_transactions (type);

CREATE TABLE project_materials (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    material_id UUID NOT NULL,
    allocated_quantity NUMERIC(12, 3) NOT NULL CHECK (allocated_quantity >= 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_project_materials_project_material UNIQUE (project_id, material_id),
    CONSTRAINT fk_project_materials_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_project_materials_material FOREIGN KEY (material_id) REFERENCES materials(id)
);

CREATE INDEX idx_project_materials_project ON project_materials (project_id);
