# ROADMAP.md — Pillars of Fortune

## Этап 1: Фундамент, FSM и регистрация игроков (MVP Core)
- [x] Инициализировать проект Gradle (`build.gradle.kts`, Java 21/25, Paper 1.21.4, Adventure, MockBukkit для тестов).
- [x] Реализовать `AGENTS.md` в корне репозитория.
- [x] Написать каркас доменных моделей: `GamePlayer`, `PlayerRole`, `PlayerRegistry`.
- [x] Реализовать FSM: `GameManager`, `GameState`, переходы между состояниями (`WAITING`, `STARTING`, `ACTIVE`, `ENDING`).
- [x] Написать `VoidTrackingListener` и перехватчик смертельного урона.
- [x] Покрыть логику переходов стейт-машины и перехвата пустоты юнит-тестами (`./gradlew test`).

## Этап 2: Процедурная генерация CubeCraft и сервис сброса арены
- [x] Реализовать `ProceduralArenaService` с поддержкой парсинга сложных блочных паттернов (`BlockPatternParser`, WorldEdit-синтаксис `60%light_blue_concrete,40%cyan_concrete`).
- [x] Реализовать генерацию центрального острова CubeCraft с декоративной окантовкой.
- [x] Реализовать динамический расчет радиуса и позиций $N$ столбов в tick 0 состояния `ACTIVE`.
- [x] Написать безопасный сборщик сущностей (удаление дропа, стрел, мобов без затрагивания игроков/спектаторов `!(entity instanceof Player)`).
- [x] Подключить чтение конфигурации карт `MapManager` (`maps/classic_neon.yml`).
- [x] Интегрировать процедурный сброс в события FSM (`STARTING`, `ENDING`).

## Этап 3: Движок лута и катастроф
- [x] Реализовать класс `WeightedLootTable` с префиксными суммами ($O(\log N)$ выборка) и покрыть его тестами.
- [x] Написать парсер и генератор `loot.yml` (материалы, зачарования, названия, веса).
- [x] Создать `ActionScheduler` для тикового управления раздачей предметов и запуском катастроф.
- [x] Реализовать пул катастроф (`MeteorShowerDisaster`, `AnvilRainDisaster`, `GhastAssaultDisaster`, `WindChargeStormDisaster`, `LevitationWaveDisaster`).
- [x] Реализовать `DisasterManager` для фильтрации и случайного выбора катастроф по измерениям и картам.

## Этап 4: Ротация, UI и полировка
- [x] Оформить весь UI через MiniMessage (ActionBars таймеров, Titles победы, Chat-сообщения выбывания).
- [x] Добавить админ-команды `/pillars start`, `/pillars stop`, `/pillars status`, `/pillars forcenext` с TabCompleter.
- [x] Написать юнит-тесты на все подсистемы (FSM, Protection, Void tracking, Loot, Parser, Scheduler, Disasters, Commands).
- [x] Собрать финальный совместимый Java 21 LTS `.jar` плагина (`NadamuPillar-1.0-SNAPSHOT.jar`).
