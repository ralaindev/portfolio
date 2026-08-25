CREATE TABLE order_lines (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    CONSTRAINT fk_order_lines_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT order_lines_quantity_positive CHECK (quantity > 0),
    CONSTRAINT order_lines_unit_price_non_negative CHECK (unit_price >= 0)
);

CREATE INDEX idx_order_lines_order_id ON order_lines (order_id);
