# Лабораторная 2 — лицензирование (Car Service)

Сервер лицензий для ПО автосервиса: PostgreSQL, JWT, подписанный тикет, операции создания, активации, проверки и продления лицензии.

Репозиторий: pagadone1/ziovpoLab, ветка zadanie2.

## Назначение

Приложение выдаёт клиентскому ПО лицензию в виде Ticket — набора полей о сроках и блокировке. Ответ TicketResponse содержит тикет и электронную подпись (SHA256withRSA), чтобы клиент мог проверить, что данные не изменены.

Администратор создаёт лицензии и продлевает их. Владелец лицензии активирует ключ на своём устройстве. Проверка лицензии только читает состояние и возвращает подписанный тикет.

## Пользователи для демо

admin / Admin1234! — создание и продление лицензий.

client / Client1234! — владелец, активация на устройстве.

## Запуск

Создайте базу photoprint и пользователя photoprint_user (скрипт database/setup-labs-run.sql в корне репозитория лаб).

Скопируйте .env.example в .env и укажите пароль к keystore: KEYSTORE_PASSWORD=changeit.

Таблицы в PgAdmin: если пусто, выполните docs/pgadmin-setup.sql на базе photoprint (8 таблиц). Иначе их создаст Hibernate при первом запуске.

Запуск:

    mvnw spring-boot:run

Сервер: https://localhost:8443

Тесты:

    mvnw test -Dspring.profiles.active=test

## API

POST /api/auth/login — вход, JWT.

POST /api/auth/refresh — обновление токена.

POST /api/license — создание лицензии (роль ADMIN).

POST /api/license/activate — активация на устройстве (владелец лицензии).

POST /api/license/check — проверка, возврат TicketResponse.

POST /api/license/renew — продление срока (только ADMIN).

Все запросы к /api/license кроме auth — с заголовком Authorization: Bearer <token>.

## Документация в проекте

docs/LAB2.md — ER-диаграмма и sequence.

postman/Lab2-License.postman_collection.json — коллекция Postman.

docs/demo-requests.http — те же запросы для IDE.

## Структура кода

models/ — сущности JPA и таблицы PostgreSQL.

service/LicenseService.java — create, activate, check, renew.

service/SignatureService.java — подпись и проверка тикета.

dto/Ticket.java, dto/TicketResponse.java — тикет и ответ с ЭЦП.

controllers/LicenseController.java — REST API лицензий.
