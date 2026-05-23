-- Лаба 2: PostgreSQL (база photoprint, пользователь photoprint_user)
-- Создание БД: database/setup-labs-run.sql
-- Таблицы создаёт Hibernate (ddl-auto=update); файл — для отчёта и PgAdmin

CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    owner_id        BIGINT REFERENCES users(id),
    username        VARCHAR(255) UNIQUE,
    email           VARCHAR(255) UNIQUE,
    password        VARCHAR(255),
    role            VARCHAR(50),
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS license_type (
    id                          BIGSERIAL PRIMARY KEY,
    name                        VARCHAR(255) NOT NULL UNIQUE,
    default_duration_in_days    INTEGER NOT NULL,
    description                 VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS license (
    id                      BIGSERIAL PRIMARY KEY,
    code                    VARCHAR(255) NOT NULL UNIQUE,
    user_id                 BIGINT REFERENCES users(id),
    product_id              BIGINT NOT NULL REFERENCES product(id),
    type_id                 BIGINT NOT NULL REFERENCES license_type(id),
    owner_id                BIGINT REFERENCES users(id),
    first_activation_date   TIMESTAMP,
    ending_date             TIMESTAMP,
    blocked                 BOOLEAN NOT NULL DEFAULT FALSE,
    device_count            INTEGER NOT NULL DEFAULT 0,
    description             TEXT,
    created_at              TIMESTAMP
);

CREATE TABLE IF NOT EXISTS device (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    mac_address     VARCHAR(255) NOT NULL UNIQUE,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS device_license (
    id              BIGSERIAL PRIMARY KEY,
    license_id      BIGINT NOT NULL REFERENCES license(id),
    device_id       BIGINT NOT NULL REFERENCES device(id),
    activation_date TIMESTAMP NOT NULL,
    UNIQUE (license_id, device_id)
);

CREATE TABLE IF NOT EXISTS license_history (
    id              BIGSERIAL PRIMARY KEY,
    license_id      BIGINT NOT NULL REFERENCES license(id),
    user_id         BIGINT REFERENCES users(id),
    status          VARCHAR(100) NOT NULL,
    change_date     TIMESTAMP,
    description     TEXT
);

CREATE TABLE IF NOT EXISTS user_sessions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id),
    refresh_token   VARCHAR(1024),
    status          VARCHAR(50),
    created_at      TIMESTAMP,
    expires_at      TIMESTAMP
);
