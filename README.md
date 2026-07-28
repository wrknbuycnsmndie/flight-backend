# Flight Backend

Backend-сервис для тестового fullstack-приложения по управлению авиарейсами.

Сервис должен быть реализован на Spring Boot и предоставлять REST API для работы с рейсами, пассажирами, аэропортами и самолётами.

---

## Стек

- Java 17
- Spring Boot 4
- Spring Web
- Spring Data JPA
- Bean Validation
- PostgreSQL
- Flyway
- Maven
- DTO для запросов и ответов
- Docker

---

## Сущности

### Flight

| Поле | Тип | Описание |
|---|---|---|
| `id` | `Long` | Идентификатор рейса |
| `flightNumber` | `String` | Номер рейса, например `SU123` |
| `departureAirportId` | `Long` | ID аэропорта вылета |
| `arrivalAirportId` | `Long` | ID аэропорта прилёта |
| `departureTime` | `LocalDateTime` | Время вылета |
| `arrivalTime` | `LocalDateTime` | Время прилёта |
| `aircraftId` | `Long` | ID самолёта |

### Airport

| Поле | Тип | Описание |
|---|---|---|
| `id` | `Long` | Идентификатор аэропорта |
| `code` | `String` | IATA-код, например `SVO` |
| `name` | `String` | Полное название |
| `city` | `String` | Город |

### Aircraft

| Поле | Тип | Описание |
|---|---|---|
| `id` | `Long` | Идентификатор самолёта |
| `model` | `String` | Модель, например `Boeing 737` |
| `capacity` | `Integer` | Вместимость |

### Passenger

| Поле | Тип | Описание |
|---|---|---|
| `id` | `Long` | Идентификатор пассажира |
| `firstName` | `String` | Имя |
| `lastName` | `String` | Фамилия |
| `passportNumber` | `String` | Номер паспорта |
| `flightId` | `Long` | ID рейса |

---

## REST API

Базовый путь:

```text
/api
```

### Рейсы

#### Получить список рейсов

```http
GET /api/flights?page=0&size=10
```

Требования:

- пагинация;
- размер страницы по умолчанию — 10 записей;
- ответ должен содержать:
  - ID;
  - номер рейса;
  - код аэропорта вылета;
  - код аэропорта прилёта;
  - время вылета;
  - количество пассажиров.

#### Получить детали рейса

```http
GET /api/flights/{id}
```

Ответ должен содержать:

- номер рейса;
- аэропорт вылета:
  - ID;
  - код;
  - полное название;
  - город;
- аэропорт прилёта:
  - ID;
  - код;
  - полное название;
  - город;
- время вылета;
- время прилёта;
- самолёт:
  - ID;
  - модель;
  - вместимость;
- количество пассажиров.

#### Создать рейс

```http
POST /api/flights
```

Пример запроса:

```json
{
  "flightNumber": "SU123",
  "departureAirportId": 1,
  "arrivalAirportId": 2,
  "departureTime": "2026-08-01T10:00:00",
  "arrivalTime": "2026-08-01T12:30:00",
  "aircraftId": 1
}
```

#### Обновить рейс

```http
PUT /api/flights/{id}
```

Тело запроса совпадает с запросом создания рейса.

#### Удалить рейс

```http
DELETE /api/flights/{id}
```

---

### Пассажиры

#### Получить пассажиров рейса

```http
GET /api/flights/{flightId}/passengers
```

Ответ должен содержать:

- ID пассажира;
- имя;
- фамилию;
- номер паспорта;
- ID рейса.

#### Добавить пассажира

```http
POST /api/flights/{flightId}/passengers
```

Пример запроса:

```json
{
  "firstName": "Ivan",
  "lastName": "Petrov",
  "passportNumber": "4010123456"
}
```

#### Удалить пассажира

```http
DELETE /api/passengers/{id}
```

---

### Справочники

#### Получить все аэропорты

```http
GET /api/airports
```

#### Получить все самолёты

```http
GET /api/aircrafts
```

---

## Валидация

Backend должен самостоятельно валидировать входящие данные.

### Flight

- все поля обязательны;
- `flightNumber` не должен быть пустым;
- аэропорт вылета должен существовать;
- аэропорт прилёта должен существовать;
- самолёт должен существовать;
- аэропорт вылета и аэропорт прилёта не должны совпадать;
- `departureTime` должен быть раньше `arrivalTime`.

### Passenger

- имя обязательно;
- фамилия обязательна;
- номер паспорта обязателен;
- рейс должен существовать.

Дополнительно рекомендуется:

- проверять уникальность номера паспорта;
- не разрешать добавление пассажиров сверх вместимости самолёта;
- не позволять менять самолёт на модель, вместимость которой меньше текущего количества пассажиров.

---

## База данных

Используется PostgreSQL.

Требования:

- PostgreSQL запускается в Docker;
- схема создаётся через Flyway;
- подключение выполняется через переменные окружения;
- Hibernate не должен создавать таблицы самостоятельно;
- приложение должно содержать тестовые данные.

Рекомендуемая настройка:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
```

### Тестовые данные

После применения миграций в базе должны находиться:

- 5 аэропортов;
- 3 самолёта;
- 10 рейсов;
- по 2–3 пассажира на каждый рейс.

### Рекомендуемые ограничения

- уникальный IATA-код аэропорта;
- вместимость самолёта больше нуля;
- аэропорт вылета не равен аэропорту прилёта;
- время вылета раньше времени прилёта;
- внешний ключ пассажира на рейс;
- каскадное удаление пассажиров при удалении рейса;
- индекс по `flight_id` в таблице пассажиров;
- индексы по внешним ключам таблицы рейсов.

---

## DTO

JPA-сущности не должны напрямую возвращаться из контроллеров.

Рекомендуемые DTO:

### Request

- `CreateFlightRequest`
- `UpdateFlightRequest`
- `CreatePassengerRequest`

### Response

- `FlightListItemResponse`
- `FlightDetailsResponse`
- `PassengerResponse`
- `AirportResponse`
- `AircraftResponse`
- `PageResponse<T>`
- `ErrorResponse`

---

## Архитектура

Рекомендуемая схема:

```text
Controller -> Service -> Repository -> PostgreSQL
```

### Controller

- принимает HTTP-запросы;
- выполняет базовую валидацию;
- вызывает сервисный слой;
- возвращает DTO;
- не содержит бизнес-логику.

### Service

- содержит бизнес-логику;
- проверяет существование связанных сущностей;
- управляет транзакциями;
- выполняет межсущностную валидацию.

### Repository

- работает с PostgreSQL через Spring Data JPA.

---

## Рекомендуемая структура пакетов

```text
wrknbuycnsmndie.flight
├── aircraft
├── airport
├── flight
│   └── dto
├── passenger
│   └── dto
├── common
│   ├── dto
│   └── exception
├── config
└── FlightApplication.java
```

Пример структуры feature-пакета:

```text
flight
├── Flight.java
├── FlightController.java
├── FlightService.java
├── FlightRepository.java
├── FlightMapper.java
└── dto
    ├── CreateFlightRequest.java
    ├── UpdateFlightRequest.java
    ├── FlightListItemResponse.java
    └── FlightDetailsResponse.java
```

---

## Обработка ошибок

Необходимо реализовать централизованную обработку исключений через:

```java
@RestControllerAdvice
```

Рекомендуемые HTTP-коды:

| Ситуация | Код |
|---|---:|
| Успешное получение или обновление | `200 OK` |
| Успешное создание | `201 Created` |
| Успешное удаление | `204 No Content` |
| Ошибка валидации | `400 Bad Request` |
| Сущность не найдена | `404 Not Found` |
| Конфликт данных | `409 Conflict` |
| Внутренняя ошибка | `500 Internal Server Error` |

Пример ответа:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Arrival time must be after departure time",
  "path": "/api/flights"
}
```

---

## Переменные окружения

Минимальный набор:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/flights
SPRING_DATASOURCE_USERNAME=flights
SPRING_DATASOURCE_PASSWORD=flights
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_FLYWAY_ENABLED=true
```

Пример конфигурации:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:validate}
    open-in-view: false

  flyway:
    enabled: ${SPRING_FLYWAY_ENABLED:true}
```

---

## Docker

Backend должен иметь multi-stage `Dockerfile`.

Требования:

- сборка приложения выполняется на первом этапе;
- runtime-образ содержит только JRE и готовый JAR;
- итоговый образ поддерживает `linux/amd64`;
- образ публикуется в Docker Hub.

Имя образа:

```text
docker.io/wrknbuycnsmndie/flight-backend:latest
```

Пример Dockerfile:

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Сборка под `linux/amd64`:

```bash
docker buildx build   --platform linux/amd64   -t wrknbuycnsmndie/flight-backend:latest   --push .
```

---

## Docker Compose

Полный `docker-compose.yml` должен содержать 3 сервиса:

- backend;
- frontend;
- PostgreSQL.

Backend должен:

- ждать готовности PostgreSQL;
- получать параметры подключения через переменные окружения;
- запускать Flyway при старте;
- быть доступным frontend-контейнеру по внутреннему имени сервиса.

Пример внутреннего адреса:

```text
http://backend:8080
```

---

## Acceptance Criteria

- [ ] Реализован список рейсов с пагинацией по 10 записей.
- [ ] Реализовано получение полной информации о рейсе.
- [ ] Реализовано создание рейса.
- [ ] Реализовано редактирование рейса.
- [ ] Реализовано удаление рейса.
- [ ] Реализовано получение пассажиров рейса.
- [ ] Реализовано добавление пассажира.
- [ ] Реализовано удаление пассажира.
- [ ] Реализован справочник аэропортов.
- [ ] Реализован справочник самолётов.
- [ ] Все обязательные поля валидируются.
- [ ] Проверяется порядок времени вылета и прилёта.
- [ ] PostgreSQL запускается в Docker.
- [ ] Flyway автоматически создаёт схему.
- [ ] Flyway автоматически добавляет тестовые данные.
- [ ] Подключение к БД выполняется через переменные окружения.
- [ ] Для API используются DTO.
- [ ] Ошибки возвращаются в едином формате.
- [ ] Backend собирается через multi-stage Dockerfile.
- [ ] Docker-образ поддерживает `linux/amd64`.
- [ ] Docker-образ опубликован в Docker Hub.
- [ ] Backend запускается в составе общего `docker-compose.yml`.

---

## Definition of Done

Backend считается готовым, когда:

1. проект собирается без ошибок;
2. PostgreSQL запускается в Docker;
3. Flyway создаёт схему и тестовые данные;
4. все REST endpoints работают;
5. невалидные запросы возвращают корректные HTTP-коды;
6. frontend получает все необходимые данные через REST API;
7. Docker-образ опубликован в Docker Hub;
8. backend запускается в составе общего `docker-compose.yml`.
