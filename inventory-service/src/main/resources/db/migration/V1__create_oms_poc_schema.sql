-- Shared oms-poc schema (inventory, order, fulfillment, notification)

CREATE TABLE products (
    id              UUID            NOT NULL PRIMARY KEY,
    sku             VARCHAR(255)    NOT NULL UNIQUE,
    title           VARCHAR(500)    NOT NULL,
    brand           VARCHAR(255),
    category        VARCHAR(1000),
    price           NUMERIC(19, 2)  NOT NULL,
    currency        VARCHAR(3)      NOT NULL,
    source_url      VARCHAR(2048),
    available_qty   INTEGER         NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL
);

CREATE TABLE stock_reservations (
    id          UUID            NOT NULL PRIMARY KEY,
    order_id    UUID            NOT NULL,
    product_id  UUID            NOT NULL REFERENCES products (id),
    quantity    INTEGER         NOT NULL,
    status      VARCHAR(50)     NOT NULL,
    created_at  TIMESTAMPTZ     NOT NULL,
    updated_at  TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_stock_reservations_order_id ON stock_reservations (order_id);
CREATE INDEX idx_stock_reservations_product_id ON stock_reservations (product_id);

CREATE TABLE orders (
    id              UUID            NOT NULL PRIMARY KEY,
    customer_id     UUID            NOT NULL,
    status          VARCHAR(50)     NOT NULL,
    total_amount    NUMERIC(19, 2)  NOT NULL,
    currency        VARCHAR(3)      NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL
);

CREATE TABLE order_items (
    id                      UUID            NOT NULL PRIMARY KEY,
    order_id                UUID            NOT NULL REFERENCES orders (id),
    product_id              UUID            NOT NULL,
    product_title_snapshot  VARCHAR(500)    NOT NULL,
    quantity                INTEGER         NOT NULL,
    unit_price              NUMERIC(19, 2)  NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);

CREATE TABLE shipments (
    id                UUID            NOT NULL PRIMARY KEY,
    order_id          UUID            NOT NULL,
    status            VARCHAR(50)     NOT NULL,
    carrier           VARCHAR(255),
    tracking_number   VARCHAR(255),
    created_at        TIMESTAMPTZ     NOT NULL,
    updated_at        TIMESTAMPTZ     NOT NULL
);

CREATE TABLE shipment_events (
    id           UUID            NOT NULL PRIMARY KEY,
    shipment_id  UUID            NOT NULL REFERENCES shipments (id),
    status       VARCHAR(50)     NOT NULL,
    occurred_at  TIMESTAMPTZ     NOT NULL,
    note         VARCHAR(1000)
);

CREATE INDEX idx_shipments_order_id ON shipments (order_id);
CREATE INDEX idx_shipment_events_shipment_id ON shipment_events (shipment_id);

CREATE TABLE notification_log (
    id          UUID            NOT NULL PRIMARY KEY,
    order_id    UUID            NOT NULL,
    channel     VARCHAR(50)     NOT NULL,
    message     VARCHAR(2000)   NOT NULL,
    status      VARCHAR(50)     NOT NULL,
    created_at  TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_notification_log_order_id ON notification_log (order_id);
