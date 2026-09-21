# Specification: Procedural Arena Generation & Reset

This document specifies the procedural generation of CubeCraft-style arenas and the asynchronous reset engine using FastAsyncWorldEdit (FAWE).

---

## 1. Dynamic Circular Geometry

In tick 0 of the `ACTIVE` state, the exact number of active players $N$ is determined:
- The circular radius $R$ is calculated based on the configured distance $D$ between adjacent players:
  $$R = \max\left(10.0, \frac{N \times D}{2\pi}\right)$$
- For each player $i \in \{0, \dots, N-1\}$, the polar angle is:
  $$\theta_i = \frac{2\pi i}{N}$$
- The center coordinate of pillar $i$ is:
  $$X_i = X_{\text{center}} + R \cos(\theta_i), \quad Z_i = Z_{\text{center}} + R \sin(\theta_i)$$

---

## 2. Pillar and Island Structure

1. **Pillar Footprint**:
   - Each pillar is built as a square of dimension $\text{size} \times \text{size}$ (default $3 \times 3$) centered at $(X_i, Y_{\text{surface}}, Z_i)$.
   - Surface layer: parsed from `surface` pattern (e.g., `60%light_blue_concrete,40%cyan_concrete`).
   - Body layers (depth 1 to $\text{depth}-1$): parsed from `body` pattern.
   - Bottom tip: obsidian block to prevent accidental destruction from below.
2. **Central Platform**:
   - Optional circular island of radius $R_{\text{center}}$ centered at $(X_{\text{center}}, Y_{\text{surface}} + Y_{\text{offset}}, Z_{\text{center}})$.
   - Optional decorative rim along the perimeter.

---

## 3. Asynchronous Arena Reset via FAWE

When transitioning from `ENDING` back to `WAITING`:
1. **Safe Entity Cleanup**:
   - Purge drops, arrows, TNT, falling blocks, and hostile mobs without touching players:
     ```java
     world.getEntities().stream()
         .filter(entity -> !(entity instanceof Player))
         .forEach(Entity::remove);
     ```
2. **Block Reset**:
   - FAWE `EditSession` replaces all modified coordinates with `BlockTypes.AIR` asynchronously.
   - No main thread hitches or lag spikes.
