# Configuration Reference

This document explains all configuration options and customization files in **Pillars of Fortune** (`nadamu-pillar`).

---

## 1. Main Configuration: `plugins/NadamuPillar/config.yml`

The `config.yml` file governs game flow, timers, and the global world reference.

```yaml
# ==========================================================
# Pillars of Fortune (nadamu-pillar) Configuration
# ==========================================================

game:
  # Minimum number of players required to trigger the STARTING countdown.
  # If the count drops below this during the countdown, the timer cancels.
  min-players: 2

  # Countdown duration in seconds before the match begins.
  countdown-seconds: 5

  # Celebration time in seconds for the winner on the battlefield ruins
  # before the arena is cleaned up and players return to WAITING state.
  celebration-seconds: 7

  # Periodic interval in seconds for giving random weighted loot to alive players.
  loot-interval-seconds: 10

  # Interval in seconds between random arena disasters.
  disaster-interval-seconds: 30

  # Warning duration in seconds displayed via Title before a disaster strikes.
  disaster-warning-seconds: 5

world:
  # Name of the single void world where all matches and spectators reside.
  name: "world"

  # Y-coordinate threshold. Falling below this value triggers instant
  # elimination, converting the player to a spectator without the death screen.
  void-death-y: -10
```

---

## 2. Map Configuration: `plugins/NadamuPillar/maps/*.yml`

Maps define the visual style, pillar dimensions, central platform layout, and disaster whitelist. The default map is `classic_neon.yml`.

### Example Map (`maps/classic_neon.yml`):
```yaml
# MiniMessage formatted display name for titles and chat
name: "<gradient:blue:light_purple>Classic Neon</gradient>"
world: "world"

geometry:
  # Surface Y level where players stand on their pillars
  y-level: 100
  # Distance in blocks between adjacent player pillars along the circle
  distance-between-players: 14.0

pillars:
  # Pillar footprint (e.g., 3 creates a 3x3 column)
  size: 3
  # Depth of pillar downward from surface
  depth: 6
  # Surface block pattern (supports single block or WorldEdit-like percentages)
  surface: "60%light_blue_concrete,40%cyan_concrete"
  # Body block pattern
  body: "70%cyan_concrete,30%blue_concrete"
  # Bottom tip block
  bottom: "OBSIDIAN"

center:
  # Whether to generate a central island
  enabled: true
  # Radius in blocks from arena center
  radius: 6.0
  # Vertical offset relative to pillar surface Y (e.g., -2 is 2 blocks lower)
  y-offset: -2
  # Fill pattern of the center platform
  pattern: "50%magenta_concrete,50%purple_concrete"
  rim:
    # Outer decorative rim
    enabled: true
    pattern: "70%yellow_concrete,30%gold_block"

features:
  # List of allowed disasters for this map
  allowed-disasters:
    - "meteor_shower"
    - "anvil_rain"
    - "ghast_assault"
    - "wind_charge_storm"
    - "levitation_wave"
```

### Pattern Syntax (`BlockPatternParser`):
The plugin uses a weighted percentage syntax for procedural block generation:
- Single material: `"STONE"` or `"OBSIDIAN"`
- Weighted palette: `"60%light_blue_concrete,40%cyan_concrete"`
- Multi-block blend: `"50%magenta_concrete,30%purple_concrete,20%crying_obsidian"`

---

## 3. Disasters Reference

| Disaster ID | Name | Description |
|---|---|---|
| `meteor_shower` | Meteor Shower | Spawns falling fireballs and ignited TNT over the arena. |
| `anvil_rain` | Anvil Rain | Spawns falling damaged and regular anvils above players. |
| `ghast_assault` | Ghast Assault | Spawns hostile ghasts firing explosive fireballs at survivors. |
| `wind_charge_storm` | Wind Charge Storm | Launches high-velocity breeze wind charges knocking players off pillars. |
| `levitation_wave` | Levitation Wave | Applies levitation effect followed by slow falling. |

---

## 4. Commands & Administration

All administrative commands require the `nadamupillar.admin` permission:

- `/pillars start`: Force-starts the countdown (or begins the game immediately if in `STARTING`).
- `/pillars stop`: Stops the current match, removes arena blocks, and resets game state to `WAITING`.
- `/pillars status`: Prints current state, registered player count, and alive players.
- `/pillars forcenext`: Immediately triggers a random disaster from the map's allowed list.
