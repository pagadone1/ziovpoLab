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
5. Run `ZiovpoApplication` в IDEA
6. Postman: import `postman/Lab3-Signature.postman_collection.json` + environment, SSL OFF

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
