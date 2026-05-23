# Где что в коде — шпаргалка для защиты

## Что такое Ticket и зачем

**License** — запись в PostgreSQL (ключ, сроки, владелец, устройства).

**Ticket** — не таблица в БД, а **DTO для клиента**: короткий снимок «лицензия действует / не действует» + время сервера. Клиентское приложение не лезет в БД, получает только тикет.

**TicketResponse** = `ticket` + **ЭЦП** (подпись SHA256withRSA). Клиент проверяет подпись и доверяет данным.

| Файл | Назначение |
|------|------------|
| `dto/Ticket.java` | 7 полей по заданию |
| `dto/TicketResponse.java` | тикет + signature |
| `service/LicenseService.java` | метод `buildTicket()` — сборка |
| `service/SignatureService.java` | `signTicket()` / `verifyTicket()` |

---

## Кто может менять лицензию

| Действие | Кто | Где в коде |
|----------|-----|------------|
| **Создать** лицензию | только **ADMIN** | `SecurityConfig`, `LicenseController` + `@PreAuthorize`, `createLicense()` |
| **Продлить** (меняет срок в БД) | только **ADMIN** | `renewLicense()`, `hasRole('ADMIN')` |
| **Активировать** (привязка к устройству) | только **владелец** (`license.owner`) | `activateLicense()` — проверка `owner.getId() == userId` |
| **Проверить** | любой с JWT | `checkLicense()` — **не меняет** БД, только отдаёт тикет |

Один владелец на лицензию: при создании задаётся `ownerId` (пользователь `client` в демо).

---

## Операции лабы 2 — куда смотреть

| Операция | Controller | Service | DTO запроса |
|----------|------------|---------|-------------|
| Создание | `LicenseController` POST `/api/license` | `createLicense` | `LicenseCreateRequest` |
| Активация | POST `/activate` | `activateLicense` | `ActivateLicenseRequest` |
| Проверка | POST `/check` | `checkLicense` | `CheckLicenseRequest` |
| Продление | POST `/renew` | `renewLicense` | `RenewLicenseRequest` |

---

## База данных (ER)

| Сущность | Файл |
|----------|------|
| license | `models/License.java` |
| device, device_license | `Device.java`, `DeviceLicense.java` |
| license_history | `LicenseHistory.java` |
| product, license_type | `Product.java`, `LicenseType.java` |
| users | `User.java` |

Схема SQL: `docs/schema-license.sql`

---

## Безопасность и вход

| Что | Файл |
|-----|------|
| JWT login / refresh | `AuthController` |
| Фильтр Bearer | `JwtAuthenticationFilter` |
| Правила URL | `SecurityConfig` |
| Роли ADMIN / USER | `models/Role.java` |

Стартовые пользователи: `config/LicenseBootstrap.java` — **admin** / **client**

---

## Тесты (показать преподу)

| Тест | Что доказывает |
|------|----------------|
| `LicenseFlowIntegrationTest` | полный сценарий API |
| `LicenseServiceTest` | логика сервиса |
| `SignatureServiceTest` | подпись работает |
| `TicketTest` | все поля Ticket |

---

## Демо

Postman: `postman/Lab2-License.postman_collection.json`  
HTTP-файл: `docs/demo-requests.http`
