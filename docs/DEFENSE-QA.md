# Лаба 2 — вопросы на защите и ответы

## Что сдавать

- Репозиторий: https://github.com/pagadone1/ziovpoLab
- Ветка: **`zadanie2`**
- Скрин: зелёный CI (jobs test + build)
- Демо: [demo-requests.http](demo-requests.http)

## Типичные вопросы преподавателя

### 1. Где ER и таблицы PostgreSQL?

**Ответ:** JPA-сущности в `models/`, связи `@ManyToOne` / `@OneToMany`. Схема для отчёта — `docs/schema-license.sql`. БД `photoprint`, пользователь `photoprint_user` (скрипт `database/setup-labs-run.sql` в корне workspace).

### 2. Чем Ticket отличается от License?

**Ответ:** `License` — запись в БД (ключ, сроки, владелец). `Ticket` — DTO для клиента: снимок состояния лицензии + время сервера + TTL тикета. Клиент не видит всю таблицу, только подписанный тикет.

### 3. Зачем TicketResponse и ЭЦП?

**Ответ:** `TicketResponse` = `ticket` + `signature` (Base64). Подпись SHA256withRSA по JSON тикета. Клиент проверяет, что данные не подменены (`SignatureService.verifyTicket`).

### 4. Поля Ticket по заданию?

| Поле | Назначение |
|------|------------|
| serverTime | Текущее время сервера |
| ticketLifetimeSeconds | Сколько секунд тикет считать валидным (3600) |
| firstActivationDate | Дата первой активации лицензии |
| expirationDate | Дата окончания лицензии |
| userId | Кто активировал |
| deviceId | На каком устройстве |
| blocked | Заблокирована ли лицензия |

Код: `LicenseService.buildTicket`, класс `dto/Ticket.java`.

### 5. Как работает создание лицензии?

**Ответ:** ADMIN → `POST /api/license` с `productId`, `typeId`, опционально `ownerId`, `deviceCount`. Генерируется уникальный `code`, пишется `license_history` со статусом CREATED. Диаграмма: `docs/LAB2.md`.

### 6. Как работает активация?

**Ответ:** USER/ADMIN передаёт `activationKey` (code) и MAC устройства. Создаётся/находится `device`, связь `device_license`, при первой активации — `firstActivationDate`, `endingDate` = now + дни из `license_type`. Возвращается подписанный `TicketResponse`.

### 7. Проверка vs активация?

**Ответ:** **Проверка** (`/check`) — лицензия уже привязана к устройству, проверяем срок и blocked, отдаём новый подписанный тикет. **Активация** (`/activate`) — первичная привязка ключа к устройству.

### 8. Когда можно продлить?

**Ответ:** `renew` — если лицензия не активирована или до конца осталось ≤ 7 дней. К `endingDate` добавляются дни из типа лицензии.

### 9. Какие роли на эндпоинтах?

| Метод | Роль |
|-------|------|
| POST /api/license | ADMIN |
| /activate, /check, /renew | USER, ADMIN |
| /api/auth/* | без токена |

`SecurityConfig` + `@PreAuthorize` на контроллере.

### 10. Почему тема автосервис, а пакет photoprint?

**Ответ:** Историческое имя Maven-проекта; продукт в БД — **Car Service Desktop** (`LicenseBootstrap`). Домен заказов/клиентов — в PO6 (`main` — инфраструктура лабы 1).

### 11. Как запустить локально?

```text
1. PostgreSQL: БД photoprint (setup-labs-run.sql)
2. .env из .env.example (`KEYSTORE_PASSWORD=changeit` для `certs/keystore.p12`)
3. mvnw spring-boot:run
4. https://localhost:8443
5. Логин admin / Admin1234!
```

### 12. Как доказать, что тесты проходят?

`mvnw test -Dspring.profiles.active=test` — unit + `LicenseFlowIntegrationTest` (полный сценарий) + `SignatureServiceTest` (подпись).

## Что НЕ путать на защите

- Лаба 1 = `main` (JWT, HTTPS, CI), без домена тикетов.
- Лаба 2 = `zadanie2` (лицензии), не путать Ticket IT-support с Ticket лицензии.
- `check` в задании = получение информации о лицензии через подписанный тикет.
