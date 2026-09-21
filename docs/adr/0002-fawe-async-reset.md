# ADR-0002: Asynchronous Arena Reset via FastAsyncWorldEdit (FAWE)

## Context and Problem
During gameplay, the arena undergoes significant block destruction from explosions, falling anvils, and player placements. Restoring blocks using standard Bukkit synchronous methods (`Block#setType()`) freezes the main server thread, causing noticeable lag spikes for players.

## Considered Options
1. **In-Memory Block History Rollback**: Track placed/broken blocks and restore them synchronously.
2. **Full World Reload**: Unload and reload the entire world directory from disk.
3. **FAWE Asynchronous EditSession**: Use FastAsyncWorldEdit to asynchronously purge and reset blocks.

## Decision
Use **FastAsyncWorldEdit `EditSession`** for rapid, non-blocking arena reset:
- Purge all non-player entities prior to block reset:
  ```java
  world.getEntities().stream()
      .filter(entity -> !(entity instanceof Player))
      .forEach(Entity::remove);
  ```
- Clear all modified arena blocks using an asynchronous `EditSession`.
- Suppress physics updates during reset to prevent cascading recalculations.

## Consequences
- **Positive**: Sub-second arena reset with zero server thread freezes or TPS drops.
- **Negative / Constraints**: Hard runtime dependency on FastAsyncWorldEdit (FAWE).
