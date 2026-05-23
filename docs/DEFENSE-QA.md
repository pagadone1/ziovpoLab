# Вопросы на защите — лаба 2

## Сдача

Репозиторий `ziovpoLab`, ветка **`zadanie2`**, зелёный CI, демо `docs/demo-requests.http`.

---

### 1. Где таблицы и ER?

`models/` + `docs/schema-license.sql`. Связи: license ↔ product, type, user, device через device_license, история в license_history.

### 2. Ticket — какие поля?

serverTime, ticketLifetimeSeconds, firstActivationDate, expirationDate, userId, deviceId, blocked — класс `dto/Ticket.java`, сборка в `LicenseService.buildTicket`.

### 3. Зачем TicketResponse?

Передача клиенту подписанного тикета: `ticket` + `signature` (SHA256withRSA, Base64). Подделка без приватного ключа невозможна.

### 4. Создание лицензии

ADMIN, `POST /api/license` → код, product, type, лимит устройств, запись в license_history (CREATED).

### 5. Активация

Ключ + MAC устройства → привязка device_license, даты активации/окончания, ответ TicketResponse.

### 6. Проверка

`POST /api/license/check` по MAC — валидация срока и привязки, новый подписанный тикет.

### 7. Продление

Если не активирована или до конца ≤ 7 дней — продление endingDate на срок типа лицензии.

### 8. Роли

Создание — только ADMIN. Остальные операции лицензий — USER и ADMIN. JWT в заголовке `Authorization: Bearer ...`.

### 9. Почему есть auth/login?

JWT нужен для вызова защищённых эндпоинтов лицензий (из лабы 1, минимум login + refresh).

### 10. Как запустить?

PostgreSQL `photoprint`, `.env` с `KEYSTORE_PASSWORD=changeit`, `mvnw spring-boot:run`, логин admin.
