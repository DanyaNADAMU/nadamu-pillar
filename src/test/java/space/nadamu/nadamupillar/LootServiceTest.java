package space.nadamu.nadamupillar;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import space.nadamu.nadamupillar.loot.LootItem;
import space.nadamu.nadamupillar.loot.LootService;
import space.nadamu.nadamupillar.loot.WeightedLootTable;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LootServiceTest {
    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Dynamic loot loads Minecraft items respecting defaults and disabled overrides")
    void testDynamicLootLoading(@TempDir File tempDir) throws IOException {
        File lootFile = new File(tempDir, "loot.yml");
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("defaults.enabled", true);
        yaml.set("defaults.weight", 10.0);
        yaml.set("defaults.stack-1", 1);
        yaml.set("defaults.stack-16.min", 1);
        yaml.set("defaults.stack-16.max", 2);
        yaml.set("defaults.stack-64.min", 1);
        yaml.set("defaults.stack-64.max", 2);

        yaml.set("items.COMMAND_BLOCK.enabled", false);
        yaml.set("items.BARRIER.enabled", false);

        yaml.set("items.SNOWBALL.weight", 25.0);
        yaml.set("items.SNOWBALL.min", 2);
        yaml.set("items.SNOWBALL.max", 3);

        yaml.set("items.BREEZE_ROD.weight", 15.0);
        yaml.set("items.BREEZE_ROD.max", 1);

        yaml.set("items.HAY_BLOCK.weight", 20.0);
        yaml.set("items.HAY_BLOCK.min", 1);
        yaml.set("items.HAY_BLOCK.max", 2);

        yaml.save(lootFile);

        LootService lootService = new LootService(tempDir, null);
        lootService.loadLoot();

        WeightedLootTable<LootItem> table = lootService.getLootTable();
        assertFalse(table.isEmpty());

        // Check disabled items are NOT in the table
        boolean hasCommandBlock = false;
        boolean hasBarrier = false;
        Optional<LootItem> snowballItem = Optional.empty();
        Optional<LootItem> breezeRodItem = Optional.empty();
        Optional<LootItem> hayBlockItem = Optional.empty();
        Optional<LootItem> diamondItem = Optional.empty(); // Unconfigured 64-stack item

        for (LootItem item : table.getItems()) {
            if (item.getMaterial() == Material.COMMAND_BLOCK) hasCommandBlock = true;
            if (item.getMaterial() == Material.BARRIER) hasBarrier = true;
            if (item.getMaterial() == Material.SNOWBALL) snowballItem = Optional.of(item);
            if (item.getMaterial() == Material.BREEZE_ROD) breezeRodItem = Optional.of(item);
            if (item.getMaterial() == Material.HAY_BLOCK) hayBlockItem = Optional.of(item);
            if (item.getMaterial() == Material.DIAMOND) diamondItem = Optional.of(item);
        }

        assertFalse(hasCommandBlock, "COMMAND_BLOCK should be disabled");
        assertFalse(hasBarrier, "BARRIER should be disabled");

        // Check configured overrides
        assertTrue(snowballItem.isPresent(), "SNOWBALL should be present");
        assertEquals(25.0, snowballItem.get().getWeight());
        assertEquals(2, snowballItem.get().getMinAmount());
        assertEquals(3, snowballItem.get().getMaxAmount());

        assertTrue(breezeRodItem.isPresent(), "BREEZE_ROD should be present");
        assertEquals(15.0, breezeRodItem.get().getWeight());
        assertEquals(1, breezeRodItem.get().getMaxAmount());

        assertTrue(hayBlockItem.isPresent(), "HAY_BLOCK should be present");
        assertEquals(20.0, hayBlockItem.get().getWeight());
        assertEquals(1, hayBlockItem.get().getMinAmount());
        assertEquals(2, hayBlockItem.get().getMaxAmount());

        // Check default unconfigured item inherits defaults
        assertTrue(diamondItem.isPresent(), "DIAMOND should be present in dynamic loot");
        assertEquals(10.0, diamondItem.get().getWeight());
        assertEquals(1, diamondItem.get().getMinAmount());
        assertEquals(2, diamondItem.get().getMaxAmount()); // stack-64 max is 2
    }

    @Test
    @DisplayName("Whitelist mode (defaults.enabled: false) only loads items with explicit enabled: true")
    void testWhitelistMode(@TempDir File tempDir) throws IOException {
        File lootFile = new File(tempDir, "loot.yml");
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("defaults.enabled", false);
        yaml.set("defaults.weight", 10.0);
        yaml.set("defaults.stack-64.min", 1);
        yaml.set("defaults.stack-64.max", 2);

        // Explicitly enabled
        yaml.set("items.COBBLESTONE.enabled", true);
        yaml.set("items.COBBLESTONE.weight", 30.0);

        // Not explicitly enabled (should inherit false from defaults.enabled)
        yaml.set("items.DIRT.weight", 50.0);

        yaml.save(lootFile);

        LootService lootService = new LootService(tempDir, null);
        lootService.loadLoot();

        WeightedLootTable<LootItem> table = lootService.getLootTable();
        assertEquals(1, table.size());
        LootItem item = table.getItems().iterator().next();
        assertEquals(Material.COBBLESTONE, item.getMaterial());
        assertEquals(30.0, item.getWeight());
    }
}
