package space.nadamu.nadamupillar.loot;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import org.bukkit.Material;

public class LootService {
    private final File lootFile;
    private final Logger logger;
    private final WeightedLootTable<LootItem> lootTable = new WeightedLootTable<>();
    private final Random random = new Random();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public LootService(File dataFolder, Logger logger) {
        this.lootFile = new File(dataFolder, "loot.yml");
        this.logger = logger != null ? logger : Logger.getLogger(LootService.class.getName());
    }

    public void loadLoot() {
        lootTable.clear();

        if (!lootFile.exists()) {
            createDefaultLootFile();
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(lootFile);
        List<Map<?, ?>> itemsList = config.getMapList("loot");

        if (itemsList.isEmpty()) {
            logger.warning("No items found in loot.yml! Populating default in-memory loot.");
            populateDefaultLoot();
            return;
        }

        int loaded = 0;
        for (Map<?, ?> entry : itemsList) {
            try {
                String matName = String.valueOf(entry.get("material"));
                Material material = Material.matchMaterial(matName.toUpperCase());
                if (material == null) {
                    logger.fine("Skipping unknown material on this server version: " + matName);
                    continue;
                }

                int min = entry.containsKey("min") ? ((Number) entry.get("min")).intValue() : 1;
                int max = entry.containsKey("max") ? ((Number) entry.get("max")).intValue() : min;
                double weight = entry.containsKey("weight") ? ((Number) entry.get("weight")).doubleValue() : 10.0;
                String name = entry.containsKey("name") ? String.valueOf(entry.get("name")) : null;

                List<String> lore = new ArrayList<>();
                if (entry.containsKey("lore") && entry.get("lore") instanceof List<?> list) {
                    for (Object l : list) {
                        lore.add(String.valueOf(l));
                    }
                }

                Map<Enchantment, Integer> enchantments = new LinkedHashMap<>();
                if (entry.containsKey("enchantments") && entry.get("enchantments") instanceof Map<?, ?> encMap) {
                    for (Map.Entry<?, ?> encEntry : encMap.entrySet()) {
                        String encName = String.valueOf(encEntry.getKey()).toLowerCase();
                        int level = ((Number) encEntry.getValue()).intValue();
                        Enchantment ench = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(encName));
                        if (ench != null) {
                            enchantments.put(ench, level);
                        }
                    }
                }

                lootTable.add(new LootItem(material, min, max, weight, name, lore, enchantments), weight);
                loaded++;
            } catch (Exception e) {
                logger.warning("Error parsing loot item: " + e.getMessage());
            }
        }

        logger.info("Loaded " + loaded + " loot items from loot.yml (Total weight: " + lootTable.getTotalWeight() + ")");
    }

    private void populateDefaultLoot() {
        // Essential combat & utility
        addSafeLoot("IRON_SWORD", 1, 1, 15.0);
        addSafeLoot("DIAMOND_SWORD", 1, 1, 5.0);
        addSafeLoot("BOW", 1, 1, 12.0);
        addSafeLoot("ARROW", 8, 16, 20.0);
        addSafeLoot("CROSSBOW", 1, 1, 8.0);
        addSafeLoot("SHIELD", 1, 1, 10.0);

        // Fun modern 1.21 items (if present)
        addSafeLoot("MACE", 1, 1, 3.0);
        addSafeLoot("WIND_CHARGE", 2, 5, 12.0);

        // Mobility & Survival
        addSafeLoot("ENDER_PEARL", 1, 2, 10.0);
        addSafeLoot("WATER_BUCKET", 1, 1, 15.0);
        addSafeLoot("SNOWBALL", 8, 16, 20.0);
        addSafeLoot("GOLDEN_APPLE", 1, 2, 8.0);
        addSafeLoot("COOKED_BEEF", 4, 8, 20.0);

        // Building blocks & sabotage
        addSafeLoot("COBBLESTONE", 16, 32, 30.0);
        addSafeLoot("OAK_PLANKS", 16, 32, 25.0);
        addSafeLoot("TNT", 1, 3, 10.0);
        addSafeLoot("FLINT_AND_STEEL", 1, 1, 8.0);
        addSafeLoot("COBWEB", 2, 4, 12.0);
        addSafeLoot("SLIME_BLOCK", 2, 4, 10.0);
    }

    private void addSafeLoot(String matName, int min, int max, double weight) {
        Material mat = Material.matchMaterial(matName);
        if (mat != null) {
            lootTable.add(new LootItem(mat, min, max, weight, null, null, null), weight);
        }
    }

    private void createDefaultLootFile() {
        try (var stream = getClass().getClassLoader().getResourceAsStream("loot.yml")) {
            if (stream != null) {
                java.nio.file.Files.copy(stream, lootFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return;
            }
        } catch (Exception ignored) {
        }

        YamlConfiguration yaml = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();

        addConfigItem(list, "COBBLESTONE", 16, 32, 30.0, null);
        addConfigItem(list, "OAK_PLANKS", 16, 32, 25.0, null);
        addConfigItem(list, "IRON_SWORD", 1, 1, 15.0, "<gradient:gold:yellow>Железный клинок</gradient>");
        addConfigItem(list, "WATER_BUCKET", 1, 1, 15.0, "<aqua>Спасительное ведро</aqua>");
        addConfigItem(list, "WIND_CHARGE", 2, 6, 14.0, "<blue>Заряд ветра</blue>");
        addConfigItem(list, "BOW", 1, 1, 12.0, null);
        addConfigItem(list, "ARROW", 8, 16, 20.0, null);
        addConfigItem(list, "SNOWBALL", 8, 16, 20.0, null);
        addConfigItem(list, "ENDER_PEARL", 1, 2, 10.0, "<dark_purple>Жемчуг Края</dark_purple>");
        addConfigItem(list, "SHIELD", 1, 1, 10.0, null);
        addConfigItem(list, "TNT", 1, 3, 10.0, null);
        addConfigItem(list, "FLINT_AND_STEEL", 1, 1, 8.0, null);
        addConfigItem(list, "GOLDEN_APPLE", 1, 2, 8.0, "<yellow>Золотое яблоко</yellow>");
        addConfigItem(list, "MACE", 1, 1, 3.0, "<gold><bold>Ударная Булава</bold></gold>");
        addConfigItem(list, "DIAMOND_SWORD", 1, 1, 5.0, "<aqua>Алмазный меч</aqua>");

        yaml.set("loot", list);
        try {
            yaml.save(lootFile);
        } catch (Exception e) {
            logger.warning("Could not create default loot.yml: " + e.getMessage());
        }
    }

    private void addConfigItem(List<Map<String, Object>> list, String material, int min, int max, double weight, String name) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("material", material);
        map.put("min", min);
        map.put("max", max);
        map.put("weight", weight);
        if (name != null) {
            map.put("name", name);
        }
        list.add(map);
    }

    public ItemStack rollLoot() {
        if (lootTable.isEmpty()) {
            populateDefaultLoot();
        }
        LootItem item = lootTable.sample(random);
        return item.createItemStack();
    }

    public void giveRandomLoot(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        ItemStack item = rollLoot();
        var leftover = player.getInventory().addItem(item);
        for (ItemStack drop : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }

        // Actionbar notification and pleasant chime sound
        player.sendActionBar(MINI_MESSAGE.deserialize("<green>+ Получен предмет: </green><white>" + item.getType().name() + " x" + item.getAmount() + "</white>"));
    }

    public WeightedLootTable<LootItem> getLootTable() {
        return lootTable;
    }
}
