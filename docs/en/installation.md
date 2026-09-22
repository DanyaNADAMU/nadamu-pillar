# Installation & Server Setup Guide

This guide details the recommended server configuration for running **Pillars of Fortune** (`nadamu-pillar`) in production.

---

## 1. System Requirements & Software

- **Java**: 21 LTS or higher (e.g. Amazon Corretto 21, Eclipse Temurin 21).
- **Server Platform**: [Paper](https://papermc.io/) 1.20.5+ / 1.21.4 (Purpur and modern Paper forks are also supported).
- **Mandatory Dependencies**:
  - [FastAsyncWorldEdit (FAWE)](https://intellectualsites.github.io/download/fawe.html) for Paper 1.21.
  - *Why FAWE is required*: The plugin procedurally builds $N$ pillars and central platforms in real-time and resets blocks asynchronously after matches without freezing the main server thread.

---

## 2. World Configuration: Dedicated Void World

Pillars of Fortune is designed around a **Single-World Void Architecture**. The game operates exclusively in the default `world`.

### Why a Void World is Crucial:
1. **Eliminating Terrain Lag**: Normal Minecraft terrain generation consumes significant CPU and RAM, creates disk I/O spikes, and triggers costly lighting updates. In a void world, empty chunks load instantly and require almost zero memory.
2. **Predictable Void Fall Mechanics**: The plugin intercepts falls at $Y = -10$ (`world.void-death-y`), instantly transforming falling players into spectators and teleporting them to safety. If normal terrain or oceans existed below the arena, players would hit land or water instead of entering the void.
3. **Rock-solid 20.0 TPS**: Without ticking blocks, fluid flows, or unmanaged mob spawns, the server maintains optimal tick rates even under heavy combat and explosive disasters.

### How to Configure a Void World

#### Method A: Vanilla Flat World Settings (Recommended)
Edit `server.properties` in your server root:
```properties
level-name=world
level-type=minecraft\:flat
generator-settings={"biome"\:"minecraft\:the_void","layers"\:[{"block"\:"minecraft\:air","height"\:1}]}
generate-structures=false
spawn-animals=false
spawn-monsters=false
spawn-npcs=false
difficulty=normal
pvp=true
```
> [!IMPORTANT]
> If the server was already started and `world/` or `world_nether/` folders exist on disk, Paper **will not** regenerate the world from scratch.
> Make sure to:
> 1. Stop the server (`docker compose stop pillar`).
> 2. Delete the `world/` and `world_nether/` directories.
> 3. Update `server.properties` and start the server again. A clean void world will be generated automatically.

#### Method B: Using Void World Generators
Alternatively, you can use a void generator plugin (such as *VoidGen* or *CleanroomGenerator*) or place a pre-made void world save into the server root.

---

## 3. Disabling Unnecessary Dimensions (Nether & The End)

By default, Paper initializes three separate dimensions: `world`, `world_nether`, and `world_the_end`. For Pillars of Fortune, the Nether and The End are completely unused. Disabling them saves significant RAM and eliminates unneeded chunk tickers.

### Step 1: Disable the Nether
Open `server.properties` and set:
```properties
allow-nether=false
```

### Step 2: Disable The End
Open `bukkit.yml` and set `allow-end` to `false`:
```yaml
settings:
  allow-end: false
```

### Step 3: Remove Existing Dimension Folders
If the server previously generated `world_nether/` and `world_the_end/`, you can safely delete those directories while the server is stopped to recover disk space.

---

## 4. Paper Performance Tuning

To ensure maximum responsiveness during multiplayer matches, adjust the following settings:

### `config/paper-world-defaults.yml`
```yaml
anticheat:
  obfuscation:
    items:
      enable: false # Not needed for minigames, saves CPU
chunks:
  auto-save-interval: -1 # Matches are temporary; disable frequent chunk disk writes
entities:
  spawning:
    # Vanilla natural mob spawning can be turned off; disasters spawn their own mobs
    monsters: -1
    animals: -1
environment:
  optimize-explosions: true # Speeds up meteor shower and TNT disasters
```

---

## 5. Plugin Installation Steps

1. Stop your Minecraft server.
2. Download and install **FastAsyncWorldEdit (FAWE)** into `plugins/`.
3. Copy `NadamuPillar-1.0-SNAPSHOT.jar` into `plugins/`.
4. Start the server.
5. Verify successful loading in the console:
   ```
   [PillarsPlugin] Pillars of Fortune plugin enabled!
   ```
6. Join the server. You should immediately spawn as a spectator above the center of the arena.
