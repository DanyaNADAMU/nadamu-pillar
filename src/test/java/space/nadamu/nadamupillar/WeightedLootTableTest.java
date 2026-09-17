package space.nadamu.nadamupillar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import space.nadamu.nadamupillar.loot.WeightedLootTable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class WeightedLootTableTest {

    @Test
    @DisplayName("Empty loot table behavior")
    void testEmptyTable() {
        WeightedLootTable<String> table = new WeightedLootTable<>();
        assertTrue(table.isEmpty());
        assertEquals(0.0, table.getTotalWeight());
        assertThrows(IllegalStateException.class, () -> table.sample(new Random()));
    }

    @Test
    @DisplayName("Single item table always returns that item")
    void testSingleItemTable() {
        WeightedLootTable<String> table = new WeightedLootTable<>();
        table.add("SWORD", 10.0);

        assertFalse(table.isEmpty());
        assertEquals(10.0, table.getTotalWeight());

        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            assertEquals("SWORD", table.sample(random));
        }
    }

    @Test
    @DisplayName("Weighted sampling distribution roughly matches relative weights")
    void testWeightedDistribution() {
        WeightedLootTable<String> table = new WeightedLootTable<>();
        table.add("COMMON", 80.0);
        table.add("RARE", 20.0);

        Random random = new Random(42);
        Map<String, Integer> counts = new HashMap<>();
        int totalTrials = 10000;

        for (int i = 0; i < totalTrials; i++) {
            String item = table.sample(random);
            counts.put(item, counts.getOrDefault(item, 0) + 1);
        }

        int commonCount = counts.getOrDefault("COMMON", 0);
        int rareCount = counts.getOrDefault("RARE", 0);

        // 80% expected ~ 8000, 20% expected ~ 2000
        assertTrue(commonCount > 7500 && commonCount < 8500, "Common count was: " + commonCount);
        assertTrue(rareCount > 1500 && rareCount < 2500, "Rare count was: " + rareCount);
    }

    @Test
    @DisplayName("Clearing table resets total weight and state")
    void testClear() {
        WeightedLootTable<String> table = new WeightedLootTable<>();
        table.add("ITEM1", 5.0);
        table.add("ITEM2", 10.0);
        assertEquals(15.0, table.getTotalWeight());

        table.clear();
        assertTrue(table.isEmpty());
        assertEquals(0.0, table.getTotalWeight());
    }
}
