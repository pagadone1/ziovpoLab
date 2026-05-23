
## Запуск

1. Скопируйте `.env.example` → `.env` и задайте пароли.
2. `keystore.p12` в `src/main/resources/` (локальный HTTPS, не в git).
3. `docker compose up --build` — PostgreSQL и приложение на `https://localhost:8443`.

Локальная БД (без Docker): PostgreSQL `itsupp_db`, пользователь `itsupp_user`.

## Секреты и CI

GitHub Actions: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`.  
Локально — `.env`.

Pipeline: jobs **test** и **build** (см. `.github/workflows/build.yml`).

## Безопасность (из РБПО PO6)

- JWT access + refresh, сессии в `UserSession`
- Роли: `ROLE_ADMIN`, `ROLE_USER` — `SecurityConfig`
- HTTPS: порт 8443, PKCS12 keystore

## Тема

Сервис для отслеживания и обработки IT-тикетов внутри компании.

## Основные сущности

- Ticket — тикет (инцидент, запрос или проблема)
- Users — пользователь системы
- Executor — сотрудник, выполняющий тикет
- Category — категория тикета
- SLA — время реакции и решения

## Операции

- CRUD для сущностей
- Назначение тикета исполнителю
- Закрытие тикета с указанием решения
- Просроченные тикеты и эскалация
- Тикеты конкретного исполнителя
