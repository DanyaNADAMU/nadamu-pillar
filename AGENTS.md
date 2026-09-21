# AGENTS.md — Pillars of Fortune

## 1. Стек технологий
- **Java**: 21 (LTS, Amazon Corretto)
- **Платформа**: Paper API 1.20.5+ / 1.21.4
- **Форматирование сообщений**: Adventure API (Component & MiniMessage)
- **Сброс арены**: FastAsyncWorldEdit (FAWE)
- **Сборщик**: Gradle (Kotlin DSL `build.gradle.kts`)
- **Тестирование**: MockBukkit v1.21, JUnit 5

## 2. Команды верификации
Перед любым рапортом о завершении задачи обязательно выполнять:
```bash
./gradlew check
./gradlew test
```

## 3. Архитектурные инварианты (Строгие запреты)
1. **Интерфейс и текст**:
   - Категорический запрет на `org.bukkit.ChatColor`.
   - Весь текст выводится исключительно через Adventure API: `net.kyori.adventure.text.Component` и `MiniMessage.miniMessage().deserialize(...)`.
2. **Перемещение игроков**:
   - Любые телепортации производятся **только** асинхронно через `player.teleportAsync(Location)`.
   - Синхронные вызовы `player.teleport(Location)` строго запрещены.
3. **Безопасность очистки сущностей**:
   - При сбросе мира или очистке сущностей никогда не вызывать `entity.remove()` на игроках:
     `!(entity instanceof Player)`.
4. **Слушатели событий (Listeners)**:
   - Запрещено динамически регистрировать и отменять регистрацию слушателей внутри состояний FSM.
   - Все слушатели регистрируются один раз в `PillarsPlugin#onEnable()` и опрашивают состояние через `GameManager#getCurrentState()`.
5. **Внедрение зависимостей (Dependency Injection)**:
   - Запрещены статические синглтоны `getInstance()` в доменных сервисах и стейт-машине.
   - Экземпляры `GameManager`, `PlayerRegistry`, `ArenaResetService` и т.д. передаются строго через конструкторы.
6. **Non-Goals (Границы скоупа)**:
   - Никаких внешних БД (MySQL/PostgreSQL) — сессия работает in-memory.
   - Никакой динамической генерации миров во время матча (используются предзагруженные статические пустотные миры).
   - Никаких NMS-пакетов (`net.minecraft.server`) — только публичные Paper API и FAWE API.

## 4. Окружение и Контейнеризация
- **Изоляция в контейнере**: Агент функционирует внутри изолированного Docker-контейнера (`antigravity-cli`).
- **Неизменяемость хоста и контейнера**: Агент не имеет прав и возможности напрямую изменять конфигурацию Docker хоста, файлы `compose.yml` или `Dockerfile.agy` (директория `/stacks/antigravity` примонтирована в read-only).
- **Запрос модификаций**: При необходимости установки новых системных утилит, библиотек или изменения параметров контейнера агент обязан запросить пользователя внести правки на хосте.
- **Полная отвязка от агента `pi`**: Агент Antigravity полностью автономен. Запрещено использовать или создавать каталоги, конфиги и переменные, привязанные к `pi` (все учетные данные Git хранятся в `/root/.git-credentials`).

## 5. Стиль кода
- Комментарии в коде — лаконичные и исключительно на английском языке.
- Явная обработка ошибок и краевых случаев (отключение игрока, падение ниже лимита мира, пустой список лута).

## 6. Навигация и документация
- **Глобальные стандарты**: `/root/.gemini/config/rules/` (`00-environment.md`, `10-project-structure.md`, `20-code-standards.md`).
- **Живая архитектура**: [`docs/architecture/`](docs/architecture/) (`overview.md`, `fsm-lifecycle.md`, `procedural-arena.md`, и др.).
- **Архитектурные решения**: [`docs/adr/`](docs/adr/) (`0001-single-void-world.md`, `0002-fawe-async-reset.md`, `0003-navigable-map-loot-distribution.md`).
- **Планы и дорожная карта**: [`docs/plans/`](docs/plans/) (`roadmap.md`).
- **Идеи и бэклог**: [`docs/ideas/`](docs/ideas/) (`backlog.md`).
- **Гайды по установке и настройке**:
  - English: [`docs/en/installation.md`](docs/en/installation.md), [`docs/en/configuration.md`](docs/en/configuration.md), [`docs/en/releases.md`](docs/en/releases.md).
  - Русский: [`docs/ru/installation.md`](docs/ru/installation.md), [`docs/ru/configuration.md`](docs/ru/configuration.md), [`docs/ru/releases.md`](docs/ru/releases.md).

