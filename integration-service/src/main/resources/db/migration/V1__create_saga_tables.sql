CREATE TABLE saga_instances (
    id            UUID            NOT NULL PRIMARY KEY,
    current_step  VARCHAR(50)     NOT NULL,
    status        VARCHAR(50)     NOT NULL,
    created_at    TIMESTAMPTZ     NOT NULL,
    updated_at    TIMESTAMPTZ     NOT NULL
);
