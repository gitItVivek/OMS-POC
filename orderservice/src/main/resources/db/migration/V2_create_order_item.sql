-- Create order item table
CREATE TABLE order_item
(
    id                  BIGSERIAL PRIMARY KEY,

    order_id            BIGINT NOT NULL,

    product_id          BIGINT NOT NULL,

    product_name        VARCHAR(255),

    quantity            INTEGER NOT NULL,

    unit_price          NUMERIC(12,2),

    total_price         NUMERIC(12,2),

    FOREIGN KEY(order_id)
        REFERENCES "order"(id)
);

-- Indexes
CREATE INDEX idx_order_item
    ON order_item(order_id);