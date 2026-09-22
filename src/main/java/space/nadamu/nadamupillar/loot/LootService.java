package space.nadamu.nadamupillar.loot;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

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

        // Check if new defaults/items structure exists
        if (config.contains("defaults") || config.contains("items")) {
            loadConfigDefaultsAndItems(config);
            return;
        }

        // Legacy loot list fallback
        List<Map<?, ?>> itemsList = config.getMapList("loot");
        if (!itemsList.isEmpty()) {
            loadLegacyLoot(itemsList);
            return;
        }

        logger.warning("No items or defaults found in loot.yml! Populating default in-memory loot.");
        populateDefaultLoot();
    }

    private void loadConfigDefaultsAndItems(YamlConfiguration config) {
        boolean defaultEnabled = config.getBoolean("defaults.enabled", true);
        double defaultWeight = config.getDouble("defaults.weight", 10.0);

        int stack1Min = 1;
        int stack1Max = 1;
        if (config.isConfigurationSection("defaults.stack-1")) {
            stack1Min = config.getInt("defaults.stack-1.min", 1);
            stack1Max = config.getInt("defaults.stack-1.max", 1);
        } else {
            int val = config.getInt("defaults.stack-1", 1);
            stack1Min = val;
            stack1Max = val;
        }

        int stack16Min = 1;
        int stack16Max = 2;
        if (config.isConfigurationSection("defaults.stack-16")) {
            stack16Min = config.getInt("defaults.stack-16.min", 1);
            stack16Max = config.getInt("defaults.stack-16.max", 2);
        } else {
            int val = config.getInt("defaults.stack-16", 2);
            stack16Min = 1;
            stack16Max = val;
        }

        int stack64Min = 1;
        int stack64Max = 2;
        if (config.isConfigurationSection("defaults.stack-64")) {
            stack64Min = config.getInt("defaults.stack-64.min", 1);
            stack64Max = config.getInt("defaults.stack-64.max", 2);
        } else {
            int val = config.getInt("defaults.stack-64", 2);
            stack64Min = 1;
            stack64Max = val;
        }

        // Parse items section overrides
        Map<Material, ItemOverride> overrides = new HashMap<>();
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                Material mat = Material.matchMaterial(key.toUpperCase());
                if (mat == null) {
                    logger.fine("Unknown material in loot items: " + key);
                    continue;
                }

                ItemOverride override = new ItemOverride();
                if (itemsSection.isBoolean(key)) {
                    override.enabled = itemsSection.getBoolean(key);
                } else if (itemsSection.isConfigurationSection(key)) {
                    ConfigurationSection sec = itemsSection.getConfigurationSection(key);
                    if (sec.contains("enabled")) override.enabled = sec.getBoolean("enabled");
                    if (sec.contains("weight")) override.weight = sec.getDouble("weight");
                    if (sec.contains("min")) override.min = sec.getInt("min");
                    if (sec.contains("max")) override.max = sec.getInt("max");
                    override.customName = sec.getString("name", null);

                    if (sec.isList("lore")) {
                        override.lore = sec.getStringList("lore");
                    }

                    if (sec.isConfigurationSection("enchantments")) {
                        ConfigurationSection encSec = sec.getConfigurationSection("enchantments");
                        override.enchantments = new LinkedHashMap<>();
                        for (String encKey : encSec.getKeys(false)) {
                            Enchantment ench = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(encKey.toLowerCase()));
                            if (ench != null) {
                                override.enchantments.put(ench, encSec.getInt(encKey));
                            }
                        }
                    }
                }
                overrides.put(mat, override);
            }
        }

        int loaded = 0;
        if (defaultEnabled) {
            // Dynamic mode: all items in Minecraft (filtered by overrides and enabled status)
            for (Material mat : Material.values()) {
                if (!mat.isItem() || mat.isAir()) {
                    continue;
                }

                ItemOverride override = overrides.get(mat);
                boolean enabled = (override != null && override.enabled != null)
                        ? override.enabled
                        : defaultEnabled;
                if (!enabled) {
                    continue;
                }

                double weight = (override != null && override.weight != null)
                        ? override.weight
                        : defaultWeight;

                int maxStack = mat.getMaxStackSize();
                int defMin = maxStack <= 1 ? stack1Min : (maxStack <= 16 ? stack16Min : stack64Min);
                int defMax = maxStack <= 1 ? stack1Max : (maxStack <= 16 ? stack16Max : stack64Max);

                int min = (override != null && override.min != null) ? override.min : defMin;
                int max = (override != null && override.max != null) ? override.max : (override != null && override.min != null ? override.min : defMax);
                String name = override != null ? override.customName : null;
                List<String> lore = override != null ? override.lore : null;
                Map<Enchantment, Integer> enchs = override != null ? override.enchantments : null;

                lootTable.add(new LootItem(mat, min, max, weight, name, lore, enchs), weight);
                loaded++;
            }
        } else {
            // Whitelist mode: only items explicitly configured in items section
            for (Map.Entry<Material, ItemOverride> entry : overrides.entrySet()) {
                Material mat = entry.getKey();
                ItemOverride override = entry.getValue();

                // Inherit enabled from defaultEnabled (false) if not specified
                boolean enabled = override.enabled != null ? override.enabled : defaultEnabled;
                if (!enabled) {
                    continue;
                }

                double weight = override.weight != null ? override.weight : defaultWeight;
                int maxStack = mat.getMaxStackSize();
                int defMin = maxStack <= 1 ? stack1Min : (maxStack <= 16 ? stack16Min : stack64Min);
                int defMax = maxStack <= 1 ? stack1Max : (maxStack <= 16 ? stack16Max : stack64Max);

                int min = override.min != null ? override.min : defMin;
                int max = override.max != null ? override.max : (override.min != null ? override.min : defMax);

                lootTable.add(new LootItem(mat, min, max, weight, override.customName, override.lore, override.enchantments), weight);
                loaded++;
            }
        }

        logger.info("Loaded " + loaded + " loot items from loot.yml (defaults.enabled=" + defaultEnabled + ", Total weight: " + lootTable.getTotalWeight() + ")");
    }

    private void loadLegacyLoot(List<Map<?, ?>> itemsList) {
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
        logger.info("Loaded " + loaded + " loot items from legacy loot.yml (Total weight: " + lootTable.getTotalWeight() + ")");
    }

    private void populateDefaultLoot() {
        addSafeLoot("COBBLESTONE", 1, 2, 25.0);
        addSafeLoot("OAK_LOG", 1, 2, 15.0);
        addSafeLoot("SNOWBALL", 2, 3, 25.0);
        addSafeLoot("EGG", 2, 3, 25.0);
        addSafeLoot("FISHING_ROD", 1, 1, 20.0);
        addSafeLoot("BOW", 1, 1, 15.0);
        addSafeLoot("ARROW", 2, 4, 25.0);
        addSafeLoot("SHIELD", 1, 1, 20.0);
        addSafeLoot("MACE", 1, 1, 18.0);
        addSafeLoot("BREEZE_ROD", 1, 1, 15.0);
        addSafeLoot("WATER_BUCKET", 1, 1, 18.0);
        addSafeLoot("ENDER_PEARL", 1, 1, 15.0);
        addSafeLoot("HAY_BLOCK", 1, 2, 20.0);
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
        yaml.set("defaults.enabled", true);
        yaml.set("defaults.weight", 10.0);
        yaml.set("defaults.stack-1", 1);
        yaml.set("defaults.stack-16.min", 1);
        yaml.set("defaults.stack-16.max", 2);
        yaml.set("defaults.stack-64.min", 1);
        yaml.set("defaults.stack-64.max", 2);

        yaml.set("items.COMMAND_BLOCK.enabled", false);
        yaml.set("items.BARRIER.enabled", false);
        yaml.set("items.ENDER_DRAGON_SPAWN_EGG.enabled", false);
        yaml.set("items.WITHER_SPAWN_EGG.enabled", false);

        yaml.set("items.SNOWBALL.weight", 25.0);
        yaml.set("items.SNOWBALL.min", 2);
        yaml.set("items.SNOWBALL.max", 3);

        yaml.set("items.EGG.weight", 25.0);
        yaml.set("items.EGG.min", 2);
        yaml.set("items.EGG.max", 3);

        yaml.set("items.FISHING_ROD.weight", 20.0);
        yaml.set("items.SHIELD.weight", 20.0);
        yaml.set("items.MACE.weight", 18.0);
        yaml.set("items.BREEZE_ROD.weight", 15.0);
        yaml.set("items.BREEZE_ROD.max", 1);
        yaml.set("items.WATER_BUCKET.weight", 18.0);
        yaml.set("items.ENDER_PEARL.weight", 15.0);
        yaml.set("items.ENDER_PEARL.min", 1);
        yaml.set("items.ENDER_PEARL.max", 1);
        yaml.set("items.HAY_BLOCK.weight", 20.0);
        yaml.set("items.HAY_BLOCK.min", 1);
        yaml.set("items.HAY_BLOCK.max", 2);

        try {
            yaml.save(lootFile);
        } catch (Exception e) {
            logger.warning("Could not create default loot.yml: " + e.getMessage());
        }
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

        // Actionbar notification
        player.sendActionBar(MINI_MESSAGE.deserialize("<green>+ Получен предмет: </green><white>" + item.getType().name() + " x" + item.getAmount() + "</white>"));
    }

    public WeightedLootTable<LootItem> getLootTable() {
        return lootTable;
    }

    private static class ItemOverride {
        Boolean enabled;
        Double weight;
        Integer min;
        Integer max;
        String customName;
        List<String> lore;
        Map<Enchantment, Integer> enchantments;
    }
}
