# Car Service — лабораторная 1

Новый репозиторий серверной части: https://github.com/pagadone1/ziovpoLab (ветка `main`).

Сюда перенесено из РБПО (`PO6`): JWT access/refresh, роли, HTTPS, PostgreSQL, секреты, CI (test + build).

**Тема проекта (автосервис):** клиенты, автомобили, механики, запчасти, заказы на ремонт — полный код в `PO6/demo`.  
В `main` только инфраструктура лабы 1, без чужого домена (тикеты, SLA и т.п. убраны).

## Запуск

`.env.example` → `.env`, `keystore.p12` в `src/main/resources/`, затем:

```bash
docker compose up --build
```

https://localhost:8443

## API в этой ветке

| Назначение | Пути |
|------------|------|
| Регистрация / вход / refresh | `POST /api/auth/register`, `/login`, `/refresh` |
| Пользователи (только ADMIN) | `GET/POST/PUT/DELETE /api/users` |

## Что сдавать по лабе 1

См. [docs/LAB1.md](docs/LAB1.md) — чеклист и ER/UML.

Домен автосервиса для отчёта: `PO6` (Customer, Vehicle, ServiceOrder…).
