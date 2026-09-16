DROP INDEX IF EXISTS idx_users_email_lower;

CREATE UNIQUE INDEX uk_users_email_lower ON users (LOWER(email));

ALTER TABLE customers
    ADD CONSTRAINT uk_customers_phone_number UNIQUE (phone_number);
