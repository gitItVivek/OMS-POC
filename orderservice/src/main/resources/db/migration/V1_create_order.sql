-- Create order table
CREATE TABLE "order"
(
    id              BIGSERIAL PRIMARY KEY,

    order_number    VARCHAR(30) UNIQUE NOT NULL,

    customer_id     BIGINT NOT NULL,

    order_status    VARCHAR(30) NOT NULL,

    total_amount    NUMERIC(12,2) NOT NULL,

    currency        VARCHAR(10) DEFAULT 'INR',

    payment_status  VARCHAR(30),

    order_date      BIGINT NOT NULL,

    created_by      BIGINT NOT NULL,

    created_at      BIGINT NOT NULL,

    updated_by      BIGINT NOT NULL,

    updated_at      BIGINT NOT NULL,

    deleted_by      BIGINT,

    deleted_at      BIGINT
);

-- Indexes
CREATE INDEX idx_orders_customer
    ON "order" (customer_id);

CREATE INDEX idx_orders_status
    ON "order" (order_status);

CREATE INDEX idx_orders_date
    ON "order" (order_date);