# Лабораторная работа 1

Проект: **Car Service (автосервис)**  
GitHub: https://github.com/pagadone1/ziovpoLab (ветка `main`)

Инфраструктура (JWT, роли, HTTPS, PostgreSQL, CI) перенесена из РБПО — репозиторий `PO6`.

## Задание

| Требование | Где |
|------------|-----|
| Git-репозиторий | `ziovpoLab`, ветка `main` |
| JWT access / refresh | `AuthService`, `/api/auth/*` |
| Роли и доступ | `SecurityConfig` |
| HTTPS | `keystore.p12`, `application.properties` |
| PostgreSQL | `docker-compose.yml`, `.env` |
| Секреты | `.env`, GitHub Secrets |
| CI test + build | `.github/workflows/build.yml` |
| UML | [uml-overview.md](uml-overview.md) |
| ER | [er-diagram.md](er-diagram.md) |

## Запуск

`.env.example` → `.env`, `docker compose up --build` → https://localhost:8443

Полный домен автосервиса (CRUD заказов и т.д.) — в `PO6/demo`.
