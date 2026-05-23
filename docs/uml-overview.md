# UML — автосервис

Кратко по типам диаграмм (лаба 1 — теория).

| Диаграмма | Пример для Car Service |
|-----------|-------------------------|
| Use Case | Клиент создаёт заказ, механик закрывает заказ |
| Class | Customer, Vehicle, ServiceOrder |
| Sequence | login → JWT → создание ServiceOrder |
| Component | API, Security, PostgreSQL |

```mermaid
sequenceDiagram
    participant C as Client
    participant A as AuthController
    participant O as ServiceOrderController
    participant DB as PostgreSQL

    C->>A: POST /api/auth/login
    A->>DB: проверка User
    A-->>C: access + refresh
    C->>O: POST /api/service-orders
    O->>DB: save order
    O-->>C: 201 Created
```

Подробная реализация сценариев — в `PO6/demo`.
