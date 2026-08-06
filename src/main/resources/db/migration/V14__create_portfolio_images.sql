CREATE TABLE portfolio_images (
    id UUID PRIMARY KEY,
    uploaded_by_user_id UUID NOT NULL,
    title VARCHAR(160) NOT NULL,
    category VARCHAR(80) NOT NULL,
    description VARCHAR(500) NOT NULL DEFAULT '',
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(80) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    image_data BYTEA NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_portfolio_images_uploaded_by FOREIGN KEY (uploaded_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_portfolio_images_created_at ON portfolio_images(created_at DESC);
