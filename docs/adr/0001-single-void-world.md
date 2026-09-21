# ADR-0001: Single Default Void World Architecture

## Context and Problem
Creating dynamic temporary worlds (via `WorldCreator` or multi-dimension splits like Nether/End) complicates architecture, requires coordinating cross-world teleports, increases memory footprint, and introduces risks of chunk/memory leaks upon unloading.

## Considered Options
1. **Dynamic World Creation per Match**: Generate an isolated world for each match and delete it afterwards.
2. **Three Persistent Dimensions**: Maintain separate Overworld, Nether, and End worlds.
3. **Single Persistent Void World**: Run all phases in the default `world`.

## Decision
Use **exactly one persistent void world — the default `world`**:
- All game states (`WAITING`, `STARTING`, `ACTIVE`, `ENDING`) execute within this single world.
- Joining players immediately spawn as spectators above the arena center.
- Dimensions like Nether and End are disabled in `server.properties` and `bukkit.yml`.

## Consequences
- **Positive**: Steady 20.0 TPS, minimal RAM usage, instantaneous local teleports, simplified server administration.
- **Negative / Constraints**: Exactly one concurrent match per server instance.
