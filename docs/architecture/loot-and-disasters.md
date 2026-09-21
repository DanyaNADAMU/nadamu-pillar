# Specification: Loot & Disaster Engine

This document details the weighted loot distribution system and the disaster execution framework.

---

## 1. Weighted Loot Distribution: $O(\log N)$

The `WeightedLootTable` utilizes a prefix-sum data structure (`java.util.NavigableMap<Double, LootItem>`):
- Each item has an associated positive weight $w_i$.
- Cumulative sum key: $C_k = \sum_{i=1}^{k} w_i$, with total weight $W = C_N$.
- A uniform pseudo-random number $r \in [0, W)$ is drawn.
- The item is resolved via `map.ceilingEntry(r)` in $O(\log N)$ time.

```java
public LootItem getRandomItem() {
    double r = ThreadLocalRandom.current().nextDouble() * totalWeight;
    return cumulativeMap.ceilingEntry(r).getValue();
}
```

---

## 2. Disaster Framework

Each disaster implements the `Disaster` interface:
```java
public interface Disaster {
    String getId();
    Set<Environment> getAllowedEnvironments();
    int getWarningDurationSeconds();
    void execute(DisasterContext context);
}
```

### Implemented Disasters:
1. `MeteorShowerDisaster`: Spawns falling fireballs and primed TNT over a 20-block radius.
2. `AnvilRainDisaster`: Spawns falling anvils 15 blocks above each alive player.
3. `GhastAssaultDisaster`: Spawns flying ghasts targeting alive players with explosive fireballs.
4. `WindChargeStormDisaster`: Launches breeze wind charges at player positions to displace them.
5. `LevitationWaveDisaster`: Applies Levitation II (5s) followed by Slow Falling (10s).
