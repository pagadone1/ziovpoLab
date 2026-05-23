# Лабораторная 3 — ЭЦП для лицензий (Car Service)

Ветка **lab3** в репозитории pagadone1/ziovpoLab.

Сервер: JWT, HTTPS, PostgreSQL, лицензии (лаба 2) + **модуль электронной подписи** для Ticket (JCS + SHA256withRSA).

## Задание лабы 3

- PKCS12-хранилище с приватным ключом и сертификатом
- Секреты CI: KEYSTORE_BASE64, KEYSTORE_PASSWORD, SIGNATURE_PUBLIC_KEY_PEM
- Пакет signature: KeyProvider, Canonicalization, SigningService
- TicketSignatureService подключает ЭЦП к activate / check / renew

Подробнее: docs/LAB3.md

## Быстрый старт

1. Скопировать `.env.example` → `.env`, заполнить пароли
2. Положить `keystore.p12` в `src/main/resources/`
3. `application.properties.example` → `application.properties`
4. PostgreSQL: база `ziovpo_db` (docker: порт 5436) или локально
5. Run `ZiovpoApplication` в IDEA (см. ниже — сервер должен **работать**, не завершаться)
6. Postman: import `postman/Lab3-Signature.postman_collection.json` + environment, SSL OFF

### IDEA: почему «останавливается»

Сервер **должен работать постоянно**. В консоли в конце должно быть:

`Started ZiovpoApplication` и `Tomcat started on port 8443 (https)` — и **процесс не завершается**.

Если видишь `Process finished with exit code 1` — приложение **упало** (часто нет `.env` или PostgreSQL).

1. Файл `.env` в корне `ziOvpo-lab-lab3` (копия из `.env.example`)
2. PostgreSQL запущен, база `ziovpo_db` существует
3. `application.properties` скопирован из `application.properties.example`
4. Run → не нажимай Stop — красный квадрат **останавливает** сервер

Логин Postman: пароль admin = значение `ADMIN_PASSWORD` из `.env` (у тебя может быть `admin12345`, не `Admin1234!`).

Пользователи после bootstrap: **admin** (пароль из ADMIN_PASSWORD), **client** / `Client1234!`

## Тесты

    mvnw test

## GitHub Secrets

| Secret | Описание |
|--------|----------|
| KEYSTORE_BASE64 | keystore.p12 в base64 |
| KEYSTORE_PASSWORD | пароль PKCS12 |
| SIGNATURE_PUBLIC_KEY_PEM | публичный сертификат PEM |

Экспорт PEM: `scripts/export-public-key-pem.sh` (Linux/Git Bash)

## Структура кода

- `signature/` — модуль ЭЦП
- `license/service/TicketSignatureService.java` — подпись Ticket
- `license/service/LicenseService.java` — лицензии
- `license/config/DemoIds.java` — UUID для Postman
