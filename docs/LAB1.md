# Лабораторная 1

**Репозиторий:** https://github.com/pagadone1/ziovpoLab, ветка `main`  
**Тема:** Car Service (автосервис)  
**РБПО-источник:** `PO6`

## Что в этом репозитории (лаба 1)

| Критерий | Файлы |
|----------|--------|
| Новый git-репозиторий сервера | `ziovpoLab` / `main` |
| JWT access + refresh | `AuthService`, `JwtTokenUtils`, `/api/auth/*` |
| Авторизация (роли) | `SecurityConfig`, `ROLE_ADMIN` / `ROLE_USER` |
| HTTPS | `application.properties`, `keystore.p12` |
| PostgreSQL | `docker-compose.yml`, `.env` |
| Секреты | `.env`, GitHub: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD` |
| CI test + build | `.github/workflows/build.yml` |

## Что убрано как лишнее

Код из шаблона **IT Support** (не автосервис): Ticket, SLA, Category, Executor, EscalationService и их контроллеры.  
Для лабы 1 они не требуются.

## Домен автосервиса

CRUD и бизнес-операции — в `PO6/demo` (Customer, Vehicle, Mechanic, Part, ServiceOrder).

## Теория

- [er-diagram.md](er-diagram.md)
- [uml-overview.md](uml-overview.md)
