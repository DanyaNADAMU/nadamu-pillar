# Specification: FSM Lifecycle

This document describes the Finite State Machine (FSM) architecture governing the game lifecycle.

---

## 1. States and Transitions

The match transitions through four discrete states managed by `GameManager`:

| State | Entry Condition | Behavior | Exit Action |
|---|---|---|---|
| `WAITING` | Server startup or match reset | Spectators hover over arena center; PvP and block manipulation blocked. | Initializes player participant list. |
| `STARTING` | Players $\ge$ `min-players` or `/pillars start` | 5-second countdown with titles, action bars, and ticks. | Prepares player positions or aborts back to `WAITING` if players drop. |
| `ACTIVE` | Countdown reaches 0 | Tick 0 builds arena; sets players to `SURVIVAL`; starts loot and disaster schedulers. | Stops schedulers; cleans up disaster tasks. |
| `ENDING` | Alive players $\le 1$ | Displays victory titles; pauses arena for 7 seconds celebration. | Asynchronously clears arena blocks and resets players to spectators. |

---

## 2. Invariant Rules for State Handlers

1. **No Dynamic Listeners**:
   - States MUST NOT register or unregister Bukkit event listeners.
   - Listeners check `GameManager#getCurrentState()` to verify if an action is permitted.
2. **Deterministic Tick Execution**:
   - `GameManager` runs a centralized 1-second (20 ticks) Bukkit scheduler task that invokes `GameState#tick()`.
3. **Safe Player Reconnection**:
   - If a player disconnects during `ACTIVE`, their role is immediately set to `SPECTATOR`, and an elimination check is evaluated. If only 1 player remains, transition to `ENDING` triggers automatically.
