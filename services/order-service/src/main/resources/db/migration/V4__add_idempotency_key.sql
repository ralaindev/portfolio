ALTER TABLE orders
    ADD COLUMN idempotency_key VARCHAR(255),
    ADD COLUMN request_fingerprint VARCHAR(64);

ALTER TABLE orders
    ADD CONSTRAINT orders_idempotency_key_unique UNIQUE (idempotency_key),
    ADD CONSTRAINT orders_fingerprint_required_with_key CHECK (
        (idempotency_key IS NULL AND request_fingerprint IS NULL)
        OR (idempotency_key IS NOT NULL AND request_fingerprint IS NOT NULL)
    );
