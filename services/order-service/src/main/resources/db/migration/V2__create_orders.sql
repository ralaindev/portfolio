CREATE TABLE orders (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT orders_total_non_negative CHECK (total_amount >= 0),
    CONSTRAINT orders_status_not_blank CHECK (length(trim(status)) > 0)
);

CREATE INDEX idx_orders_customer_status ON orders (customer_id, status);
