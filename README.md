# Car Service — лабораторная 2 (лицензирование)

Ветка: https://github.com/pagadone1/ziovpoLab (`zadanie2`)

Тема проекта — **автосервис**. В этой ветке — модуль лицензий на ПО (учёт ключей, устройств, подписанный тикет).

Полное описание лабы: [docs/LAB2.md](docs/LAB2.md)  
ER и SQL: [docs/schema-license.sql](docs/schema-license.sql)

## Запуск

```bash
cp .env.example .env   # или скопировать вручную в Windows
mvnw test -Dspring.profiles.active=test
mvnw spring-boot:run
```

HTTPS: https://localhost:8443  
БД: PostgreSQL `photoprint`

## Реализовано по заданию

| Задача | Код |
|--------|-----|
| Таблицы и связи (ER) | `models/*`, Hibernate + `docs/schema-license.sql` |
| Создание лицензии | `POST /api/license` |
| Активация | `POST /api/license/activate` |
| Проверка | `POST /api/license/check` |
| Продление | `POST /api/license/renew` |
| `Ticket` (7 полей) | `dto/Ticket.java` |
| `TicketResponse` + ЭЦП | `dto/TicketResponse.java`, `SignatureService` |

Домен автосервиса (заказы, клиенты) — в `PO6/demo` и лабе 1 (`main` — только инфраструктура).

**Защита:** [docs/DEFENSE-QA.md](docs/DEFENSE-QA.md) · **Демо HTTP:** [docs/demo-requests.http](docs/demo-requests.http)

При первом старте создаются: admin (`Admin1234!`), продукт *Car Service Desktop*, типы STANDARD/ANNUAL.
