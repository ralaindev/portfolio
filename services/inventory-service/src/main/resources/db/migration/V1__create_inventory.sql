CREATE TABLE product_stock (
    product_id UUID PRIMARY KEY,
    available_quantity INTEGER NOT NULL,
    reserved_quantity INTEGER NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT product_stock_available_non_negative CHECK (available_quantity >= 0),
    CONSTRAINT product_stock_reserved_non_negative CHECK (reserved_quantity >= 0)
);

CREATE TABLE stock_reservations (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT stock_reservations_quantity_positive CHECK (quantity > 0),
    CONSTRAINT stock_reservations_order_product_unique UNIQUE (order_id, product_id)
);

CREATE INDEX idx_stock_reservations_product_id ON stock_reservations (product_id);
