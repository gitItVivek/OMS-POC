-- Search interest tracking and trending (order-service domain, shared oms-poc DB)

CREATE TABLE search_interests (
    id              UUID            NOT NULL PRIMARY KEY,
    customer_id     UUID            NOT NULL,
    search_query    VARCHAR(500)    NOT NULL,
    product_id      UUID            NOT NULL,
    category        VARCHAR(1000),
    created_at      TIMESTAMPTZ     NOT NULL
);

CREATE TABLE product_search_trends (
    id              UUID            NOT NULL PRIMARY KEY,
    product_id      UUID            NOT NULL,
    trend_window    VARCHAR(20)     NOT NULL,
    window_start    DATE            NOT NULL,
    search_count    INTEGER         NOT NULL DEFAULT 0,
    CONSTRAINT uq_product_search_trends UNIQUE (product_id, trend_window, window_start)
);

CREATE INDEX idx_search_interests_customer_created ON search_interests (customer_id, created_at DESC);
CREATE INDEX idx_search_interests_category ON search_interests (category);
CREATE INDEX idx_product_search_trends_lookup ON product_search_trends (trend_window, window_start, search_count DESC);
