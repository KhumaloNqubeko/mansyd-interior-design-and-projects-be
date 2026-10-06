ALTER TABLE projects ADD COLUMN completion_review_status VARCHAR(30) NOT NULL DEFAULT 'NOT_REQUESTED'
    CHECK (completion_review_status IN ('NOT_REQUESTED', 'PENDING_REVIEW', 'ISSUE_REPORTED', 'CONFIRMED'));
ALTER TABLE projects ADD COLUMN completion_review_id UUID;
ALTER TABLE projects ADD COLUMN customer_confirmed_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE projects ADD COLUMN customer_confirmed_by UUID REFERENCES users(id);

CREATE TABLE project_activity (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id),
    kind VARCHAR(30) NOT NULL CHECK (kind IN ('COMMENT', 'PHOTO', 'REVIEW_REQUESTED', 'COMPLETION_CONFIRMED', 'ISSUE_REPORTED')),
    message VARCHAR(2000) NOT NULL,
    file_name VARCHAR(255),
    content_type VARCHAR(40),
    photo_data BYTEA,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CHECK ((kind = 'PHOTO' AND photo_data IS NOT NULL AND content_type IN ('image/jpeg', 'image/png'))
        OR (kind <> 'PHOTO' AND photo_data IS NULL))
);
CREATE INDEX idx_project_activity_created ON project_activity(project_id, created_at DESC);
