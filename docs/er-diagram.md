# ER-диаграмма (домен IT Support)

## Основные понятия ER

- **Сущность** — объект предметной области (Ticket, Users).
- **Атрибут** — свойство сущности (title, email).
- **Связь** — отношение между сущностями; кратность: 1:1, 1:N, M:N.

## Схема проекта

```mermaid
erDiagram
    USERS ||--o{ USER_SESSION : has
    USERS {
        bigint id PK
        string username
        string email
        string password
        string role
    }
    USER_SESSION {
        bigint id PK
        string refresh_token
        string status
        timestamp expires_at
    }
    CATEGORY ||--o{ TICKET : classifies
    CATEGORY {
        bigint id PK
        string name
    }
    EXECUTOR ||--o{ TICKET : handles
    EXECUTOR {
        bigint id PK
        string name
        string email
        string department
    }
    SLA ||--o{ TICKET : defines_deadline
    SLA {
        bigint id PK
        string name
        int response_hours
        int resolution_hours
    }
    TICKET {
        bigint id PK
        string title
        string description
        string status
        string resolution
        timestamp created_at
        timestamp due_date
    }
```

## Связи

| Связь | Тип | Описание |
|-------|-----|----------|
| Users → UserSession | 1:N | Сессии refresh-токенов |
| Category → Ticket | 1:N | Категория тикета |
| Executor → Ticket | 1:N | Исполнитель тикета |
| SLA → Ticket | 1:N | SLA для расчёта сроков |

Пользователь (Users) создаёт тикеты через API; в текущей модели JPA связь User–Ticket задаётся на уровне бизнес-логики контроллеров.
