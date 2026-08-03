CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('CARPENTER', 'CUSTOMER')),
    account_status VARCHAR(20) NOT NULL CHECK (account_status IN ('ACTIVE', 'DISABLED', 'LOCKED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email_lower ON users (LOWER(email));
CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_created_at ON users (created_at);

CREATE TABLE customers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    phone_number VARCHAR(30) NOT NULL,
    address_line_1 VARCHAR(160) NOT NULL,
    address_line_2 VARCHAR(160),
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_customers_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_customers_user UNIQUE (user_id)
);

CREATE INDEX idx_customers_created_at ON customers (created_at);

