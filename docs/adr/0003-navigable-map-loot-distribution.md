# ADR-0003: Weighted Loot Randomization via Prefix-Sum NavigableMap

## Context and Problem
During a match, alive players periodically receive random weighted loot. Iterating sequentially through a flat list of items on each roll requires $O(N)$ operations. With many players and frequent loot intervals, linear searching introduces unnecessary CPU overhead.

## Considered Options
1. **Flat Array with Element Duplication**: Repeat item references proportional to integer weights (drawback: excessive memory usage with fractional weights).
2. **Linear Search ($O(N)$)**: Accumulate weights on each lookup.
3. **Prefix-Sum Binary Search ($O(\log N)$)**: Cumulative weight distribution in a binary tree.

## Decision
Use `java.util.NavigableMap<Double, LootItem>` where:
- Key is the cumulative weight $\sum_{i=1}^{k} w_i$.
- Total weight is $W = \sum w_i$.
- A uniform pseudo-random number $r \in [0, W)$ is generated.
- The item is resolved via `map.ceilingEntry(r)` in $O(\log N)$ time.

## Consequences
- **Positive**: Extremely fast $O(\log N)$ lookups, support for fractional floating-point weights, deterministic distribution accuracy.
- **Negative / Constraints**: Loot table rebuild required if item weights change dynamically during runtime.
