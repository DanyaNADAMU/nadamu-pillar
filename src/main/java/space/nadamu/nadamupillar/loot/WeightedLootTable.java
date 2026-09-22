package space.nadamu.nadamupillar.loot;

import java.util.NavigableMap;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;

public class WeightedLootTable<T> {
    private final NavigableMap<Double, T> distribution = new TreeMap<>();
    private double totalWeight = 0.0;
    private final Random defaultRandom = new Random();

    public synchronized void add(T item, double weight) {
        Objects.requireNonNull(item, "item cannot be null");
        if (weight <= 0.0) {
            return;
        }
        totalWeight += weight;
        distribution.put(totalWeight, item);
    }

    public synchronized T sample(Random random) {
        if (distribution.isEmpty()) {
            throw new IllegalStateException("Cannot sample from an empty WeightedLootTable");
        }
        Random rng = random != null ? random : defaultRandom;
        double r = rng.nextDouble() * totalWeight;
        var entry = distribution.ceilingEntry(r);
        if (entry != null) {
            return entry.getValue();
        }
        return distribution.lastEntry().getValue();
    }

    public T sample() {
        return sample(defaultRandom);
    }

    public synchronized boolean isEmpty() {
        return distribution.isEmpty();
    }

    public synchronized int size() {
        return distribution.size();
    }

    public synchronized double getTotalWeight() {
        return totalWeight;
    }

    public synchronized java.util.Collection<T> getItems() {
        return java.util.Collections.unmodifiableCollection(distribution.values());
    }

    public synchronized void clear() {
        distribution.clear();
        totalWeight = 0.0;
    }
}
