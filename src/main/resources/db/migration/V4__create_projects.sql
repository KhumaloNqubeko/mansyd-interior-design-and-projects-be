CREATE TABLE projects (
    id UUID PRIMARY KEY,
    project_number VARCHAR(40) NOT NULL,
    order_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    status VARCHAR(40) NOT NULL CHECK (status IN (
        'CREATED',
        'SCHEDULED',
        'IN_PROGRESS',
        'AWAITING_MATERIALS',
        'ON_HOLD',
        'QUALITY_INSPECTION',
        'READY_FOR_DELIVERY',
        'DELIVERED',
        'INSTALLED',
        'COMPLETED',
        'CANCELLED'
    )),
    progress INTEGER NOT NULL CHECK (progress >= 0 AND progress <= 100),
    planned_start_date DATE,
    planned_completion_date DATE,
    actual_completion_date DATE,
    notes VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_projects_number UNIQUE (project_number),
    CONSTRAINT uk_projects_order UNIQUE (order_id),
    CONSTRAINT fk_projects_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_projects_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE INDEX idx_projects_customer ON projects (customer_id);
CREATE INDEX idx_projects_status ON projects (status);
CREATE INDEX idx_projects_created_at ON projects (created_at);

CREATE TABLE project_updates (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    created_by_user_id UUID NOT NULL,
    title VARCHAR(140) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_project_updates_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_updates_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_project_updates_project_created ON project_updates (project_id, created_at DESC);
