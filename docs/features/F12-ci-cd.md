# F12: GitHub Actions CI/CD

**Статус:** Реализовано

## Цель

Автоматизировать проверку backend и сборку production Docker-образа через
GitHub Actions.

## Реализация

- [x] Создать workflows в `.github/workflows/ci.yml` и `.github/workflows/release.yml`.
- [x] Запускать тесты и Docker-сборку на pull request в `dev`/`main` и push в `dev`.
- [x] Публиковать production-образ только после merge в `main` или GitHub Release.
- [x] Собирать и публиковать образ для `linux/amd64` с безопасными тегами и Secrets.
- [x] Настроить cache, минимальные permissions и provenance/attestation.

## Pipeline

### CI: pull request и `dev`

1. Checkout исходного кода.
2. Установка Java 17 с Maven cache.
3. Запуск `./mvnw test`.
4. Сборка Docker-образа без публикации.

Pull request и push в `dev` должны завершаться ошибкой при падении тестов или Docker-сборки.

### Release: `main` и GitHub Release

1. Запускать только после успешной проверки.
2. Выполнить login в Docker Hub с помощью `DOCKERHUB_USERNAME` и
   `DOCKERHUB_TOKEN` из Secrets.
3. Собрать образ через `docker/build-push-action`.
4. Опубликовать образ с платформой `linux/amd64`.
5. Добавить provenance/attestation.

Минимальный набор секретов:

- `DOCKERHUB_USERNAME`;
- `DOCKERHUB_TOKEN`.

Секреты не должны попадать в Dockerfile, логи workflow или теги образов.

Workflow использует образ `docker.io/wrknbuycnsmndie/flight-backend`.

## Теги образов

- `sha-<commit>` — неизменяемая ссылка на конкретную сборку;
- `<branch>` — текущая сборка ветки;
- `latest` — только для стабильной ветки или release.

## Тесты

- [ ] `./mvnw test` и Docker-сборка успешно проходят в GitHub Actions.
- [ ] Pull request и push в `dev` не публикуют образ.
- [ ] Merge в `main` публикует рабочий `linux/amd64` образ в Docker Hub.
- [ ] Опубликованный образ запускается через `docker compose`.

## Acceptance Criteria

- Падение любого теста блокирует сборку образа.
- Pull request не имеет права публиковать Docker-образ.
- Успешный workflow доверенной ветки публикует версионированный образ.
- Образ можно скачать из Docker Hub и запустить с PostgreSQL через Compose.
