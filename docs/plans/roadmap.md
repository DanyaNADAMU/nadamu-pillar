# Project Roadmap & Execution Plan

This document tracks milestones, completed deliverables, and ongoing tasks for **Pillars of Fortune** (`nadamu-pillar`).

---

## Phase 1: Foundation, FSM & Player Registration (Core MVP)
- [x] Initialize Gradle project (`build.gradle.kts`, Java 21, Paper 1.21.4, Adventure, MockBukkit for tests).
- [x] Define local project rules in `AGENTS.md`.
- [x] Implement core domain models: `GamePlayer`, `PlayerRole`, `PlayerRegistry`.
- [x] Implement FSM: `GameManager`, `GameState`, state transitions (`WAITING`, `STARTING`, `ACTIVE`, `ENDING`).
- [x] Implement `VoidTrackingListener` and fatal damage interceptor.
- [x] Cover state machine transitions and void interception with unit tests (`./gradlew test`).

---

## Phase 2: CubeCraft Procedural Generation & Arena Reset
- [x] Implement `ProceduralArenaService` with WorldEdit-like block percentage parser (`BlockPatternParser`).
- [x] Implement procedural central island generation with decorative rim.
- [x] Implement dynamic calculation of radius and positions for $N$ pillars in tick 0 of `ACTIVE`.
- [x] Implement safe entity collector (purging drops, arrows, and mobs while preserving players `!(entity instanceof Player)`).
- [x] Implement `MapManager` for loading and validating map configurations (`maps/classic_neon.yml`).
- [x] Integrate procedural reset into FSM transitions (`STARTING`, `ENDING`).

---

## Phase 3: Loot & Disaster Engine
- [x] Implement `WeightedLootTable` with prefix-sum binary search ($O(\log N)$) and unit test coverage.
- [x] Implement parser and generator for loot definitions.
- [x] Create `ActionScheduler` for tick-based loot intervals and disaster triggers.
- [x] Implement disaster pool (`MeteorShowerDisaster`, `AnvilRainDisaster`, `GhastAssaultDisaster`, `WindChargeStormDisaster`, `LevitationWaveDisaster`).
- [x] Implement `DisasterManager` for environment and map disaster filtering.

---

## Phase 4: UI, Commands, Testing & Packaging
- [x] Style all UI elements using MiniMessage (ActionBars, Titles, Chat messages).
- [x] Implement administrative commands: `/pillars start`, `/pillars stop`, `/pillars status`, `/pillars forcenext` with TabCompleter.
- [x] Comprehensive unit test suite covering FSM, protection, void tracking, loot, parser, scheduler, disasters, and commands (23/23 tests passing).
- [x] Build compatible Java 21 LTS release jar (`NadamuPillar-1.0-SNAPSHOT.jar`).

---

## Phase 5: Future Enhancements
- [ ] Implement spectator hotbar interaction items (teleport to player, toggle speed).
- [ ] Add support for custom sound and particle packs per disaster.
- [ ] Implement multi-arena rotation from `maps/` directory.
- [ ] Add optional stats tracking (wins, kills, matches played) in an in-memory or JSON storage.
