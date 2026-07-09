CREATE TABLE pipeline_runs (
    id              UUID            NOT NULL PRIMARY KEY,
    order_id        UUID            NOT NULL,
    customer_id     UUID            NOT NULL,
    orchestration   VARCHAR(50)     NOT NULL,
    status          VARCHAR(50)     NOT NULL,
    elapsed_ms      BIGINT,
    tracking_number VARCHAR(100),
    created_at      TIMESTAMPTZ     NOT NULL,
    completed_at    TIMESTAMPTZ
);

CREATE INDEX idx_pipeline_runs_orchestration_created ON pipeline_runs (orchestration, created_at DESC);
