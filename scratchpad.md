# scratchpad.md — Рабочий буфер текущей сессии

## Текущая задача
Развертывание Context-as-Code и реализация Этапа 1 (MVP Core).

## Гипотезы и архитектурные решения
1. Билд-скрипт переводится на `build.gradle.kts` с Java 21 toolchain и Paper API 1.21.4.
2. Тестирование строится на базе `MockBukkit-v1.21` и JUnit Jupiter 5.
3. Модель `PlayerRegistry` хранит игроков сессии по `UUID` (`Map<UUID, GamePlayer>`), обеспечивая потокобезопасный доступ и изоляцию стейта от глобального Bukkit.
4. FSM управляется `GameManager`:
   - Состояния: `WaitingState`, `StartingState` (15 сек), `ActiveState`, `EndingState` (5 сек).
   - Тик стейта вызывается централизованно через задачу Bukkit scheduler (1 такт = 1 тик).
5. `VoidTrackingListener` перехватывает:
   - `EntityDamageEvent`: если `health - damage <= 0`, событие отменяется (`isCancelled = true`), игрок переводится в `SPECTATOR`.
   - `PlayerMoveEvent` / тик: если $Y < Y_{\min}$ (по умолчанию -10), игрок сбрасывает скорость, восстанавливает здоровье, переводится в спектатора и телепортируется на спавн спектаторов через `player.teleportAsync()`.

## Чеклист самопроверки
- [x] Установлен Amazon Corretto 25 JDK в контейнер
- [x] Сконфигурирован `build.gradle.kts` (Gradle 9.7.1, release 21)
- [x] Структура документации в `docs/` развернута
- [x] Удалены старые legacy-файлы
- [x] Написаны доменные модели `GamePlayer`, `PlayerRole`, `PlayerRegistry`
- [x] Написан интерфейс `GameState` и классы состояний (`WaitingState`, `StartingState`, `ActiveState`, `EndingState`)
- [x] Реализован `GameManager`
- [x] Реализован `VoidTrackingListener` и `PlayerEliminateEvent`
- [x] Реализован `GameProtectionListener`
- [x] Написаны юнит-тесты с MockBukkit (14 тестов, 100% pass)
- [x] Пройдена проверка `./gradlew check && ./gradlew test`

## Текущий статус: Выполнено (Цель достигнута)
- Все компоненты плагина `nadamu-pillar` полностью реализованы согласно спецификациям и пожеланиям пользователя.
- Все юнит-тесты проходят успешно (23 теста, 100% pass).
- Скомпилирован финальный JAR-архив `build/libs/NadamuPillar-1.0-SNAPSHOT.jar` (Java 21 LTS bytecode, формат 65.0).
- Готов к развертыванию на Paper 1.20.5+ / 1.21.4.
