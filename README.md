# Ziovpo — лицензирование ПО (lab 3)

Серверная часть: JWT, роли, HTTPS, PostgreSQL, подпись лицензионных тикетов (JCS + RSA).

## Запуск

1. Скопируйте `.env.example` в `.env` (или используйте подготовленный `.env`).
2. Положите `keystore.p12` в `src/main/resources/`.
3. Скопируйте конфиг: `application.properties.example` → `application.properties`.
4. `docker compose up --build` — API на `https://localhost:8443`.

## CI

Ветка `lab3` в [ziovpoLab](https://github.com/pagadone1/ziovpoLab): jobs **test** и **build**.  
Secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`.
