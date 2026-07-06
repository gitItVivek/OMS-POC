CREATE TABLE users (
    id              UUID            NOT NULL PRIMARY KEY,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    email           VARCHAR(255)    UNIQUE,
    phone           VARCHAR(15)     UNIQUE,
    password_hash   VARCHAR(255),
    display_name    VARCHAR(100)    NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',
    role            VARCHAR(30)     NOT NULL DEFAULT 'USER',
    is_verified     BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ
);

CREATE TABLE revoked_tokens (
    id              UUID            NOT NULL PRIMARY KEY,
    token_hash      VARCHAR(64)     NOT NULL UNIQUE,
    expires_at      TIMESTAMPTZ     NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL
);
