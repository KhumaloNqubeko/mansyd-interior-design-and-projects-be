CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    title VARCHAR(160) NOT NULL,
    category VARCHAR(30) NOT NULL CHECK (category IN ('MATERIALS', 'LABOUR', 'TRANSPORT', 'EQUIPMENT', 'SUBCONTRACTOR', 'OVERHEAD', 'OTHER')),
    status VARCHAR(30) NOT NULL CHECK (status IN ('DRAFT', 'APPROVED', 'REIMBURSED', 'VOID')),
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    expense_date DATE NOT NULL,
    receipt_reference VARCHAR(180) NOT NULL,
    notes VARCHAR(1000) NOT NULL,
    supplier_id UUID,
    project_id UUID,
    material_id UUID,
    created_by_user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_expenses_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT fk_expenses_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_expenses_material FOREIGN KEY (material_id) REFERENCES materials(id),
    CONSTRAINT fk_expenses_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_expenses_status ON expenses (status);
CREATE INDEX idx_expenses_category ON expenses (category);
CREATE INDEX idx_expenses_expense_date ON expenses (expense_date);
CREATE INDEX idx_expenses_supplier ON expenses (supplier_id);
CREATE INDEX idx_expenses_project ON expenses (project_id);
