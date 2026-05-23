-- =============================================================================
-- PgAdmin 4: создать все таблицы лабы 2 (база photoprint)
-- =============================================================================
-- 1) PgAdmin → Databases → photoprint → Query Tool
-- 2) Вставить весь файл → Execute (F5)
-- 3) Внизу должен быть список из 8 таблиц
--
-- Если базы нет — выполни блок «Создание БД» от имени postgres (отдельное окно).
-- =============================================================================

-- ---------- Создание БД (только если photoprint ещё нет; пользователь postgres) ----------
-- CREATE USER photoprint_user WITH PASSWORD 'photoprint_pass';
-- CREATE DATABASE photoprint OWNER photoprint_user;
-- GRANT ALL PRIVILEGES ON DATABASE photoprint TO photoprint_user;

-- ---------- Таблицы (порядок из-за внешних ключей) ----------

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

-- Права для пользователя приложения (если таблицы создавал postgres)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'photoprint_user') THEN
        GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO photoprint_user;
        GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO photoprint_user;
    END IF;
END $$;

-- ---------- Проверка: должно быть 8 таблиц ----------
SELECT table_name AS "Таблицы в photoprint"
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- Ожидаемые имена:
-- device, device_license, license, license_history, license_type,
-- product, user_sessions, users
