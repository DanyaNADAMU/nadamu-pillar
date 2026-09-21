# Pillars of Fortune (`nadamu-pillar`)

A high-performance, dynamic minigame plugin for Minecraft servers powered by **Paper 1.21.4** and **Java 21**.

In **Pillars of Fortune**, players spawn atop isolated pillars hovering over an endless void. Every few seconds, players receive randomized weighted loot and face environmental and magical disasters (meteor showers, falling anvils, ghast attacks, wind charges, levitation waves). The goal is to survive, knock opponents into the void, and be the last player standing.

---

## Key Features

- **Robust FSM Lifecycle**: Deterministic state machine (`WAITING` -> `STARTING` -> `ACTIVE` -> `ENDING`) with zero dynamic listener re-registration.
- **Dynamic Procedural Arena Generation**: Automatically calculates and builds $N$ player pillars and a CubeCraft-style central island using FastAsyncWorldEdit (FAWE) in tick 0.
- **Weighted Loot Engine**: $O(\log N)$ prefix-sum binary search loot table (`WeightedLootTable`) supporting arbitrary item weights, enchants, and custom display names.
- **Disaster Engine**: Extensible disaster framework featuring meteor showers, anvil rain, ghast assaults, wind charge storms, and levitation waves.
- **Void & Death Interception**: Instant spectator transition on fatal damage or falling below $Y = -10$, completely eliminating the vanilla death screen.
- **Single-World Architecture**: Designed exclusively for a single void world (`world`), eliminating multi-world sync issues and memory overhead.
- **Modern Adventure API**: 100% MiniMessage formatting without deprecated Bukkit ChatColor.

---

## Requirements

- **Java**: 21 (LTS) or higher (e.g., Amazon Corretto, Eclipse Temurin).
- **Server Software**: [Paper](https://papermc.io/) 1.20.5+ / 1.21.4.
- **Dependencies**: [FastAsyncWorldEdit (FAWE)](https://intellectualsites.github.io/download/fawe.html) for Paper 1.21.

---

## Quickstart & Installation

1. Install **FastAsyncWorldEdit (FAWE)** into your server's `plugins/` directory.
2. Build or download `NadamuPillar-1.0-SNAPSHOT.jar` and place it into `plugins/`.
3. Configure your server to use a **Void World** and disable Nether and The End for optimal performance.
4. Start your server.

> [!TIP]
> For complete step-by-step instructions on setting up a void world, disabling extra dimensions, and tuning server properties, see [Installation & Setup Guide (EN)](docs/en/installation.md) or [Руководство по установке (RU)](docs/ru/installation.md).

---

## Commands & Permissions

| Command | Description | Permission |
|---|---|---|
| `/pillars start` | Force starts the countdown or game | `nadamupillar.admin` |
| `/pillars stop` | Resets the game to `WAITING` state | `nadamupillar.admin` |
| `/pillars status` | Shows current game state and alive players | `nadamupillar.admin` |
| `/pillars forcenext` | Triggers the next random disaster immediately | `nadamupillar.admin` |

---

## Building from Source

```bash
# Clone the repository
git clone https://github.com/DanyaNADAMU/nadamu-pillar.git
cd nadamu-pillar

# Run checks and unit tests
./gradlew check
./gradlew test

# Build production jar
./gradlew build
```
The compiled jar will be available in `build/libs/NadamuPillar-1.0-SNAPSHOT.jar`.

---

## Documentation Links

- [Installation & Setup Guide (EN)](docs/en/installation.md)
- [Configuration Reference (EN)](docs/en/configuration.md)
- [Руководство по установке (RU)](docs/ru/installation.md)
- [Справочник конфигурации (RU)](docs/ru/configuration.md)
- [Architecture Overview](docs/architecture/overview.md)
- [Architecture Decision Records (ADRs)](docs/adr/)
- [Development Plans & Roadmap](docs/plans/roadmap.md)
- [Ideas & Backlog](docs/ideas/backlog.md)
