-- Create order status history table
CREATE TABLE order_status_history
(
    id                  BIGSERIAL PRIMARY KEY,

    order_id            BIGINT NOT NULL,

    previous_status     VARCHAR(30),

    current_status      VARCHAR(30),

    remarks             VARCHAR(255),

    changed_by          BIGINT NOT NULL,

    changed_at          BIGINT NOT NULL,

    FOREIGN KEY(order_id)
        REFERENCES "order"(id)
);