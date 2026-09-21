# Specification: Void & Fatal Damage Interceptor

This document details how player deaths and void falls are intercepted cleanly without vanilla death screens.

---

## 1. Problem & Goals

In vanilla Minecraft, lethal damage or void falls trigger the red "You Died!" respawn screen. This causes:
- Gameplay disruption and unneeded client respawn packets.
- Teleportation to global server spawn instead of the minigame spectator deck.
- Dropped items cluttering the void or ground.

---

## 2. Interception Mechanism (`VoidTrackingListener`)

### Fatal Damage Interception (`EntityDamageEvent`)
1. Filter: Entity is a `Player`, and `PlayerRegistry.get(uuid).getRole() == PlayerRole.ALIVE`.
2. Check: `player.getHealth() - event.getFinalDamage() <= 0.0`.
3. If true:
   - Cancel damage event: `event.setCancelled(true)`.
   - Restore health: `player.setHealth(20.0)`, clear status effects.
   - Transition player: set role to `PlayerRole.SPECTATOR` and game mode to `GameMode.SPECTATOR`.
   - Fire `PlayerEliminateEvent`.
   - Asynchronously teleport to spectator spawn: `player.teleportAsync(spectatorLocation)`.

### Void Fall Interception (`PlayerMoveEvent` / State Tick)
1. Check: `player.getLocation().getY() < voidDeathY` (default $-10.0$).
2. If true:
   - Zero velocity: `player.setVelocity(new Vector(0, 0, 0))`.
   - Switch to `GameMode.SPECTATOR`.
   - Fire `PlayerEliminateEvent`.
   - Asynchronously teleport to spectator spawn: `player.teleportAsync(spectatorLocation)`.
