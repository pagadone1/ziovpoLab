# IT Support (itsupp)

Бэкенд на Spring Boot — внутренняя техподдержка: тикеты, категории, исполнители, SLA.

Репозиторий курса: https://github.com/pagadone1/ziovpoLab (ветка `main`).

JWT, HTTPS и CI взяты из РБПО (проект PO6), доменная логика — своя.

## Как запустить

Скопировать `.env.example` в `.env`, прописать пароли.

Положить `keystore.p12` в `src/main/resources/` (в git не коммитится).

```bash
docker compose up --build
```

После старта: https://localhost:8443

Без Docker — PostgreSQL, база `itsupp_db`, пользователь `itsupp_user` (см. скрипты в папке `database` у соседних лаб, если настраивали вручную).

## Переменные

В `.env`: хост и креды БД, `JWT_SECRET`, `KEYSTORE_PASSWORD`, пароль админа.

В GitHub Actions нужны `KEYSTORE_BASE64` и `KEYSTORE_PASSWORD` — как в РБПО.

## API

Авторизация: `POST /api/auth/register`, `/api/auth/login`, `/api/auth/refresh`.

Остальное под `/api/...` — тикеты, пользователи, категории и т.д. Админские методы только с `ROLE_ADMIN`.

## Лаба 1

Чеклист и схемы: [docs/LAB1.md](docs/LAB1.md), [docs/er-diagram.md](docs/er-diagram.md), [docs/uml-overview.md](docs/uml-overview.md).

CI: `.github/workflows/build.yml` — `test`, потом `build`.
