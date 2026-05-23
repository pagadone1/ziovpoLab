# Лабораторная 2 — лицензирование

Репозиторий: https://github.com/pagadone1/ziovpoLab, ветка **`zadanie2`**  
Тема: ПО автосервиса (Car Service)

## Чеклист (критерии оценки)

| Критерий | Где реализовано |
|----------|-----------------|
| Структура таблиц PostgreSQL по ER | JPA-модели в `models/`, DDL: [schema-license.sql](schema-license.sql) |
| Создание лицензии | `LicenseService.createLicense`, `POST /api/license` (ADMIN) |
| Активация | `LicenseService.activateLicense`, `POST /api/license/activate` |
| Проверка (получение информации) | `LicenseService.checkLicense`, `POST /api/license/check` |
| Продление | `LicenseService.renewLicense`, `POST /api/license/renew` |
| Класс `Ticket` | `dto/Ticket.java` — 7 полей по заданию |
| `TicketResponse` + ЭЦП | `dto/TicketResponse.java`, `SignatureService` (SHA256withRSA, Base64) |

## ER-диаграмма

```mermaid
erDiagram
    USERS ||--o{ LICENSE : owns
    USERS ||--o{ LICENSE : activates
    USERS ||--o{ DEVICE : has
    PRODUCT ||--o{ LICENSE : for
    LICENSE_TYPE ||--o{ LICENSE : type
    LICENSE ||--o{ DEVICE_LICENSE : binds
    DEVICE ||--o{ DEVICE_LICENSE : binds
    LICENSE ||--o{ LICENSE_HISTORY : logs
    USERS ||--o{ LICENSE_HISTORY : actor
    USERS ||--o{ USER_SESSION : sessions

    USERS {
        bigint id PK
        string username
        string password
        enum role
    }
    PRODUCT {
        bigint id PK
        string name
    }
    LICENSE_TYPE {
        bigint id PK
        int default_duration_in_days
    }
    LICENSE {
        bigint id PK
        string code UK
        datetime first_activation_date
        datetime ending_date
        boolean blocked
        int device_count
    }
    DEVICE {
        bigint id PK
        string mac_address UK
    }
    DEVICE_LICENSE {
        bigint id PK
        datetime activation_date
    }
    LICENSE_HISTORY {
        bigint id PK
        string status
        string description
    }
```

## Ticket и TicketResponse

**Ticket** (`dto/Ticket.java`):

| Поле | Тип |
|------|-----|
| serverTime | `LocalDateTime` — текущая дата/время сервера |
| ticketLifetimeSeconds | `long` — время жизни тикета |
| firstActivationDate | `LocalDate` — дата активации лицензии |
| expirationDate | `LocalDate` — дата истечения |
| userId | `Long` |
| deviceId | `Long` |
| blocked | `boolean` |

**TicketResponse**: `ticket` + `signature` (ЭЦП тикета в Base64).

Сборка тикета: `LicenseService.buildTicket` → `SignatureService.signTicket`.

## Диаграммы последовательности

### Создание лицензии

```mermaid
sequenceDiagram
    participant A as Admin
    participant C as LicenseController
    participant S as LicenseService
    participant DB as PostgreSQL

    A->>C: POST /api/license
    C->>S: createLicense(request, adminId)
    S->>DB: save License, LicenseHistory
    S-->>C: License
    C-->>A: 201 Created
```

### Активация

```mermaid
sequenceDiagram
    participant U as User
    participant C as LicenseController
    participant S as LicenseService
    participant Sig as SignatureService
    participant DB as PostgreSQL

    U->>C: POST /api/license/activate
    C->>S: activateLicense(request, userId)
    S->>DB: License, Device, DeviceLicense
    S->>S: buildTicket
    S->>Sig: signTicket(ticket)
    Sig-->>S: signature Base64
    S-->>C: TicketResponse
    C-->>U: 200 OK
```

### Проверка

```mermaid
sequenceDiagram
    participant C as Client
    participant LC as LicenseController
    participant S as LicenseService
    participant Sig as SignatureService

    C->>LC: POST /api/license/check
    LC->>S: checkLicense(deviceMac)
    S->>S: validateActiveLicense
    S->>Sig: signTicket
    S-->>LC: TicketResponse
    LC-->>C: 200 OK
```

### Продление

```mermaid
sequenceDiagram
    participant U as User
    participant C as LicenseController
    participant S as LicenseService

    U->>C: POST /api/license/renew
    C->>S: renewLicense(code, userId)
    S->>S: endingDate += type.duration
    S-->>C: TicketResponse
    C-->>U: 200 OK
```

## API (кратко)

Все запросы с JWT (кроме `/api/auth/*`).

```http
POST /api/license              # ADMIN — тело LicenseCreateRequest
POST /api/license/activate     # USER, ADMIN
POST /api/license/check        # USER, ADMIN
POST /api/license/renew        # USER, ADMIN
```

## Запуск

`.env.example` → `.env`, БД `photoprint`, `mvnw spring-boot:run` или CI.

Тесты: `mvnw test -Dspring.profiles.active=test`
