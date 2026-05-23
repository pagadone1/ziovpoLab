# Лабораторная 2 — лицензирование (Car Service)

Ветка: https://github.com/pagadone1/ziovpoLab (`zadanie2`)

Сервер лицензий для ПО автосервиса: PostgreSQL, JWT, подписанный `Ticket`, операции create / activate / check / renew.

## Документация

- [docs/LAB2.md](docs/LAB2.md) — задание, ER, sequence
- [docs/DEFENSE-QA.md](docs/DEFENSE-QA.md) — вопросы на защите
- [docs/demo-requests.http](docs/demo-requests.http) — демо-запросы
- [docs/schema-license.sql](docs/schema-license.sql) — схема БД

## Запуск

```bash
# БД: database/setup-labs-run.sql → photoprint / photoprint_user
cp .env.example .env
mvnw spring-boot:run
```

https://localhost:8443 · admin / `Admin1234!`

Тесты: `mvnw test -Dspring.profiles.active=test`

## API (лаба 2)

| Метод | Путь | Роль |
|-------|------|------|
| POST | `/api/auth/login` | — |
| POST | `/api/auth/refresh` | — |
| POST | `/api/license` | ADMIN |
| POST | `/api/license/activate` | USER, ADMIN |
| POST | `/api/license/check` | USER, ADMIN |
| POST | `/api/license/renew` | USER, ADMIN |
