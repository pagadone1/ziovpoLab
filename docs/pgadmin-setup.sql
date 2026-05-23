-- Lab3: PostgreSQL ziovpo_db (см. .env)
-- PgAdmin: Query Tool на базе ziovpo_db → Execute (F5)

CREATE TABLE IF NOT EXISTS users (
    id                      UUID PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL UNIQUE,
    email                   VARCHAR(255) NOT NULL UNIQUE,
    department              VARCHAR(255),
    password_hash           VARCHAR(255) NOT NULL,
    role                    VARCHAR(255) NOT NULL,
    is_account_expired      BOOLEAN NOT NULL DEFAULT FALSE,
    is_account_locked       BOOLEAN NOT NULL DEFAULT FALSE,
    is_credentials_expired  BOOLEAN NOT NULL DEFAULT FALSE,
    is_disabled             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS product (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    is_blocked  BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS license_type (
    id                          UUID PRIMARY KEY,
    name                        VARCHAR(255) NOT NULL,
    default_duration_in_days    INTEGER NOT NULL,
    description                 VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS license (
    id                      UUID PRIMARY KEY,
    code                    VARCHAR(255) NOT NULL UNIQUE,
    product_id              UUID NOT NULL REFERENCES product(id),
    type_id                 UUID NOT NULL REFERENCES license_type(id),
    owner_id                UUID NOT NULL REFERENCES users(id),
    user_id                 UUID REFERENCES users(id),
    first_activation_date   DATE,
    ending_date             DATE,
    blocked                 BOOLEAN NOT NULL DEFAULT FALSE,
    device_count            INTEGER NOT NULL DEFAULT 0,
    description             TEXT
);

CREATE TABLE IF NOT EXISTS device (
    id              UUID PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    mac_address     VARCHAR(255) NOT NULL UNIQUE,
    user_id         UUID NOT NULL REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS device_license (
    id              UUID PRIMARY KEY,
    license_id      UUID NOT NULL REFERENCES license(id),
    device_id       UUID NOT NULL REFERENCES device(id),
    UNIQUE (license_id, device_id)
);

CREATE TABLE IF NOT EXISTS license_history (
    id              UUID PRIMARY KEY,
    license_id      UUID NOT NULL REFERENCES license(id),
    user_id         UUID REFERENCES users(id),
    status          VARCHAR(100) NOT NULL,
    change_date     DATE,
    description     TEXT
);

CREATE TABLE IF NOT EXISTS user_sessions (
    id              UUID PRIMARY KEY,
    user_id         UUID REFERENCES users(id),
    refresh_token   VARCHAR(1024),
    status          VARCHAR(50),
    created_at      TIMESTAMP,
    expires_at      TIMESTAMP
);

SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
ORDER BY table_name;
