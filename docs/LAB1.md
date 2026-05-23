# Лабораторная работа 1

Репозиторий серверной части: **IT Support (`itsupp`)**  
GitHub: https://github.com/pagadone1/ziovpoLab (ветка `main`)

Инфраструктура безопасности перенесена из проекта РБПО (PO6 — автосервис): JWT, роли, HTTPS, PostgreSQL, CI.

## Выполнение задания

| Требование | Реализация |
|------------|------------|
| Git-репозиторий сервера | Этот репозиторий, ветка `main` |
| JWT access / refresh | `JwtTokenUtils`, `AuthService`, `/api/auth/login`, `/api/auth/refresh` |
| Авторизация (роли, правила) | `SecurityConfig`, `@EnableMethodSecurity`, роли `ROLE_ADMIN`, `ROLE_USER` |
| HTTPS | `server.ssl.*` в `application.properties`, `keystore.p12` |
| PostgreSQL | `spring.datasource.*`, `docker-compose.yml`, БД `itsupp_db` |
| Секреты | `.env` (локально), GitHub Secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD` |
| CI: test и build | `.github/workflows/build.yml` — jobs `test`, `build` |
| UML (теория) | [uml-overview.md](uml-overview.md) |
| ER (теория + схема домена) | [er-diagram.md](er-diagram.md) |

## Секреты

Скопируйте `.env.example` → `.env` и задайте значения:

- `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` — PostgreSQL
- `JWT_SECRET` — не короче 32 символов (без дефисов в тестах)
- `KEYSTORE_PASSWORD` — пароль PKCS12
- `ADMIN_PASSWORD` — пароль bootstrap-админа

В GitHub (Settings → Secrets → Actions) должны быть те же секреты keystore, что и в РБПО, либо новые с перекодированием:

```bash
base64 -w0 src/main/resources/keystore.p12
```

## Запуск

```bash
docker compose up --build
```

Приложение: `https://localhost:8443`

Проверка auth:

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh  { "refresh_token": "..." }
```

## CI

Workflow **itsupp ci/cd**: на push в `main` выполняются `test` (`mvnw clean test`) и `build` (`mvnw package`, артефакт JAR).

Статус: https://github.com/pagadone1/ziovpoLab/actions
