# Лабораторная 3 — модуль ЭЦП

Ветка `lab3`, репозиторий pagadone1/ziovpoLab.

## Критерии задания

| Критерий | Где в проекте |
|----------|----------------|
| Хранилище ключей (PKCS12) | `src/main/resources/keystore.p12` |
| Ключи в CI (GitHub Secrets) | `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `SIGNATURE_PUBLIC_KEY_PEM` |
| Компоненты модуля ЭЦП | пакет `com.example.ziovpo.signature` |
| ЭЦП подключена к лицензии | `TicketSignatureService` → `LicenseService` |
| Подпись Ticket корректна | тесты `TicketSignatureServiceTest`, `LicenseFlowIntegrationTest` |

## Модуль ЭЦП

- `KeyProvider` / `KeyStoreKeyProvider` — загрузка приватного ключа и сертификата из PKCS12
- `CanonicalizationService` / `JcsCanonicalizationService` — канонизация JSON (JCS)
- `SigningService` — SHA256withRSA + Base64
- `SignatureProperties` — настройки из `application.properties`
- `TicketSignatureService` — подпись и проверка `Ticket` для лицензий

## API лицензий (лаба 2 + подпись)

`POST /api/licenses` — create (ADMIN)  
`POST /api/licenses/activate` — TicketResponse (owner)  
`POST /api/licenses/check` — TicketResponse (+ productId)  
`POST /api/licenses/renew` — TicketResponse (ADMIN)

## Демо-данные

`LicenseDemoBootstrap` — admin, client, product, типы STANDARD/ANNUAL.  
UUID для Postman: `license/config/DemoIds.java`.

## CI secrets (GitHub)

1. `KEYSTORE_BASE64` — base64 файла `keystore.p12`
2. `KEYSTORE_PASSWORD` — пароль хранилища
3. `SIGNATURE_PUBLIC_KEY_PEM` — публичный сертификат PEM (скрипт `scripts/export-public-key-pem.sh`)

## Запуск

`.env` + `application.properties` + PostgreSQL (`ziovpo_db`) + `keystore.p12`.  
IDE: Run `ZiovpoApplication`.  
Postman: `postman/Lab3-Signature.postman_collection.json`.
