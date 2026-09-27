# 🏦 Fintech Ledger Core

Backend-приложение для обработки банковских переводов, построенное на Java и Spring Boot.

Проект демонстрирует работу с финансовыми транзакциями, PostgreSQL, Redis, JWT-аутентификацией, optimistic locking и интеграционным тестированием через Testcontainers.

## 🛠 Tech Stack

- **Java 21**
- **Spring Boot 4.0.2**
- **Spring MVC**
- **Spring Data JPA / Hibernate**
- **Spring Security**
- **PostgreSQL**
- **Redis**
- **Liquibase**
- **JWT**
- **MapStruct**
- **Maven**
- **Testcontainers**
- **Spring Boot Actuator**
- **Prometheus / Micrometer**

## 🏗 Architecture

Приложение построено по классической слоистой архитектуре:

```text
Client
  │
  ▼
Controller
  │
  ▼
Service
  │
  ├── Redis
  │
  ▼
Repository
  │
  ▼
PostgreSQL
```

Основная бизнес-логика находится в service layer, HTTP API — в controllers, доступ к PostgreSQL реализован через Spring Data JPA repositories.

## 💸 Transfers

Основной endpoint:

```http
POST /api/transfers
Authorization: Bearer <JWT>
Idempotency-Key: <unique-key>
```

Пример запроса:

```json
{
  "fromId": 1,
  "toId": 2,
  "amount": 100.00
}
```

При выполнении перевода сервис:

1. Проверяет `Idempotency-Key` в Redis.
2. Проверяет существование счетов.
3. Проверяет, что отправитель владеет исходным счётом.
4. Проверяет достаточность баланса.
5. Проверяет совпадение валют счетов.
6. Изменяет балансы отправителя и получателя.
7. Создаёт запись `Transaction`.
8. Возвращает DTO результата операции.

Операция выполняется внутри `@Transactional`, поэтому изменения PostgreSQL выполняются атомарно и откатываются при ошибке транзакции.

## 🔒 Concurrency

Для сущности `Account` используется optimistic locking:

```java
@Version
private int version;
```

Hibernate учитывает версию записи при обновлении счёта. Это позволяет обнаруживать конкурентные изменения одной записи и защищает баланс от lost update.

## 🔁 Idempotency

Для повторных HTTP-запросов используется заголовок:

```http
Idempotency-Key
```

Redis хранит:

- временный маркер `PROCESSING` с TTL 30 секунд;
- сериализованный результат выполненной операции с TTL 24 часа.

При последовательном повторе запроса с уже сохранённым результатом сервис возвращает предыдущий `TransferResponseDto` вместо повторного выполнения перевода.

## 🔐 Security

Приложение использует stateless JWT-аутентификацию.

```text
Login
  ↓
JWT
  ↓
Authorization: Bearer <token>
  ↓
JwtFilter
  ↓
SecurityContext
  ↓
Controller
```

`JwtFilter` извлекает username из JWT, загружает пользователя и помещает его в `SecurityContext`.

В контроллерах текущий пользователь может быть получен через:

```java
@AuthenticationPrincipal User currentUser
```

Spring Security настроен в stateless-режиме.

Публичными являются:

```text
/api/auth/**
/actuator/**
```

Остальные endpoints требуют аутентификации.

Пароли проверяются через `PasswordEncoder`.

## 👤 Ownership Check

Перед списанием средств сервис проверяет, что исходный счёт принадлежит аутентифицированному пользователю.

Попытка выполнить перевод с чужого счёта отклоняется.

Это дополнительно проверяется интеграционным тестом `TransferSecurityIT`.

## 🗃 Data Model

Основные сущности:

### User

Хранит данные пользователя:

- username;
- email;
- password hash;
- role;
- признак системного пользователя.

### Bank

Представляет банк, к которому относится счёт.

### Account

Хранит:

- баланс;
- валюту;
- уникальный номер счёта;
- владельца;
- банк;
- version для optimistic locking.

Для денежных значений используется:

```java
BigDecimal
```

Баланс хранится с:

```text
precision = 15
scale = 2
```

### Transaction

Хранит информацию о переводе:

- сумму;
- валюту;
- тип/status операции;
- счёт отправителя;
- счёт получателя;
- время создания.

Связи с исходным и целевым счётом настроены как:

```java
@ManyToOne(fetch = FetchType.LAZY)
```

Это позволяет не загружать связанные `Account` автоматически при каждом чтении `Transaction`.

## 📚 Transaction History

Историю операций можно получить через:

```http
GET /api/accounts/{id}/transactions
Authorization: Bearer <JWT>
```

Используется Spring Data `Pageable`, поэтому история возвращается с пагинацией.

Repository выбирает операции, где указанный счёт является либо отправителем, либо получателем.

## 🔑 Authentication

Авторизация выполняется через:

```http
POST /api/auth/login
```

После успешной проверки username/password сервер генерирует JWT.

Полученный токен используется для обращения к защищённым endpoints:

```http
Authorization: Bearer <token>
```

## 🗄 Database Migrations

Схема PostgreSQL управляется через Liquibase.

Hibernate работает в режиме:

```yaml
ddl-auto: validate
```

Поэтому Hibernate проверяет соответствие entity-модели существующей схеме, а изменение структуры базы выполняется через Liquibase migrations.

## 🧪 Integration Tests

Интеграционные тесты запускают реальные сервисы через Testcontainers:

- PostgreSQL 15;
- Redis 7.2.

Это позволяет тестировать приложение не на in-memory аналоге БД, а на PostgreSQL и Redis.

В проекте присутствуют, в частности:

### TransferIdempotencyIT

Проверяет последовательный повтор запроса с одинаковым `Idempotency-Key` и отсутствие повторного списания.

### TransferRollbackIT

Проверяет, что некорректный перевод не изменяет баланс отправителя.

### TransferSecurityIT

Проверяет запрет перевода денег с чужого счёта.

> Текущий тест идемпотентности проверяет последовательный повтор запроса. Конкурентный сценарий с двумя одновременно выполняющимися запросами с одинаковым ключом отдельно не проверяется.

## 📊 Observability

Подключены:

- Spring Boot Actuator;
- Micrometer;
- Prometheus registry.

В конфигурации доступны endpoints:

```text
/actuator/health
/actuator/prometheus
```

Метрики публикуются с тегом приложения:

```text
application=fintech-ledger
```

## 🎯 What This Project Demonstrates

Проект используется для практики и демонстрации:

- REST API design;
- Spring MVC;
- Spring Security;
- JWT authentication;
- Spring Data JPA;
- Hibernate dirty checking;
- PostgreSQL transactions;
- optimistic locking;
- Redis-based idempotency/cache;
- Liquibase migrations;
- MapStruct mapping;
- pagination;
- integration testing with Testcontainers;
- metrics with Actuator and Prometheus.

## ⚠️ Current Limitations

Проект является учебным и содержит несколько областей для дальнейшего улучшения:

- усиление идемпотентности через PostgreSQL unique constraint;
- атомарная обработка конкурентных `Idempotency-Key`;
- запись результата в Redis только после успешного DB commit;
- отдельная модель состояния финансовой операции;
- расширение тестов конкурентными сценариями;
- использование публичного номера счёта вместо внутренних database ID в transfer API.
