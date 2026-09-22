# Configuration Reference

This document explains all configuration options, maps, loot systems, and administrative commands in **Pillars of Fortune** (`nadamu-pillar`).

---

## 1. Main Configuration: `plugins/NadamuPillar/config.yml`

The `config.yml` file governs game flow, timers, auto-start behavior, and the global world reference.

```yaml
# ==========================================================
# Pillars of Fortune (nadamu-pillar) Configuration
# ==========================================================

game:
  # Minimum number of players required to trigger the STARTING countdown.
  min-players: 2

  # Countdown duration in seconds before the match begins.
  countdown-seconds: 5

  # Celebration time in seconds for the winner on the battlefield ruins
  # before the arena is cleaned up and players return to WAITING state.
  celebration-seconds: 7

  # Periodic interval in seconds for giving random loot to alive players.
  loot-interval-seconds: 5

  # Interval in seconds between random arena disasters.
  disaster-interval-seconds: 30

  # Warning duration in seconds displayed via Title before a disaster strikes.
  disaster-warning-seconds: 5

  # Automatically start match when min-players is reached.
  # Calling /pillars stop automatically turns this off.
  auto-start: true

  # Default map ID loaded upon startup.
  default-map: "classic_bedrock"

world:
  # Name of the single void world where all matches and spectators reside.
  name: "world"

  # Y-coordinate threshold. In modern Minecraft (1.18+), building extends to -64.
  # Falling below -70 triggers instant elimination without the death screen.
  void-death-y: -70
```

---

## 2. Map Configuration: `plugins/NadamuPillar/maps/*.yml`

Maps define pillar dimensions, block patterns, and the allowed disasters list.
The plugin includes two built-in maps:
* `classic_bedrock.yml` — classic $1 \times 1$ bedrock pillars without a center island.
* `classic_neon.yml` — modern $3 \times 3$ neon concrete pillars with a central platform.

### Example Map `classic_bedrock.yml` ($1 \times 1$, no center):
```yaml
name: "<gray><bold>Classic Bedrock</bold></gray>"
world: "world"

geometry:
  # Surface Y level where players stand on their pillars
  y-level: 100
  # Distance in blocks between adjacent player pillars along the circle
  distance-between-players: 14.0

pillars:
  # Pillar footprint (1 creates a 1x1 column)
  size: 1
  # Depth of pillar downward from surface
  depth: 5
  # Surface, body, and bottom block patterns
  surface: "BEDROCK"
  body: "BEDROCK"
  bottom: "BEDROCK"

center:
  # Central island disabled
  enabled: false

features:
  # List of allowed disasters for this map
  allowed-disasters:
    - "meteor_shower"
    - "anvil_rain"
    - "ghast_assault"
    - "wind_charge_storm"
    - "levitation_wave"
```

---

## 3. Loot Configuration: `plugins/NadamuPillar/loot.yml`

The loot system uses a **Default Policy (`defaults`)** and **Overrides (`items`)** architecture:

```yaml
defaults:
  # true  = dynamic loot (all Minecraft items drop, except items with enabled: false)
  # false = whitelist mode (only items with explicit enabled: true drop)
  enabled: true
  weight: 10.0
  stack-1: 1          # items with maxStackSize 1 (shield, bucket, tools)
  stack-16:           # items with maxStackSize 16 (pearls, snowballs, eggs)
    min: 1
    max: 2
  stack-64:           # items with maxStackSize 64 (blocks, arrows, food)
    min: 1
    max: 2

items:
  # 1. Disabled items (cannot place or use in Survival)
  COMMAND_BLOCK:
    enabled: false
  BARRIER:
    enabled: false
  ENDER_DRAGON_SPAWN_EGG:
    enabled: false

  # 2. Weight and amount overrides
  SNOWBALL:
    weight: 25.0
    min: 2
    max: 3
  SHIELD:
    weight: 20.0
  MACE:
    weight: 18.0
  BREEZE_ROD:
    weight: 15.0
    max: 1
  HAY_BLOCK:
    weight: 20.0
    min: 1
    max: 2
```

### Behavior:
* Missing properties in `items` inherit from `defaults`.
* When `defaults.enabled: false`, an item is only added to the pool if it explicitly sets `enabled: true`.

---

## 4. Disasters Reference

| Disaster ID | Name | Description |
|---|---|---|
| `meteor_shower` | Meteor Shower | Spawns falling fireballs and ignited TNT over the arena. |
| `anvil_rain` | Anvil Rain | Spawns falling damaged and regular anvils above players. |
| `ghast_assault` | Ghast Assault | Spawns hostile ghasts firing explosive fireballs at survivors. |
| `wind_charge_storm` | Wind Charge Storm | Launches high-velocity breeze wind charges knocking players off pillars. |
| `levitation_wave` | Levitation Wave | Applies levitation effect followed by slow falling. |

---

## 5. Commands & Administration

All administrative commands require the `nadamupillar.admin` permission:

- `/pillars start [seconds]`: Starts the match with a countdown.
- `/pillars stop`: Stops the match, clears arena blocks, sets all players to spectators, and **disables auto-start**.
- `/pillars autostart <on|off|toggle>`: Toggles or sets automatic match starting when enough players join.
- `/pillars status`: Displays current state, auto-start status, and player counts.
- `/pillars forcenext`: Skips countdown (in `STARTING`) or checks win conditions (in `ACTIVE`).
