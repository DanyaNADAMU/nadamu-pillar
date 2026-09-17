package space.nadamu.nadamupillar.loot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class LootItem {
    private final Material material;
    private final int minAmount;
    private final int maxAmount;
    private final double weight;
    private final String customName;
    private final List<String> lore;
    private final Map<Enchantment, Integer> enchantments;

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Random RANDOM = new Random();

    public LootItem(
            Material material,
            int minAmount,
            int maxAmount,
            double weight,
            String customName,
            List<String> lore,
            Map<Enchantment, Integer> enchantments
    ) {
        this.material = Objects.requireNonNull(material, "material cannot be null");
        this.minAmount = Math.max(1, minAmount);
        this.maxAmount = Math.max(this.minAmount, maxAmount);
        this.weight = Math.max(0.01, weight);
        this.customName = customName;
        this.lore = lore != null ? List.copyOf(lore) : List.of();
        this.enchantments = enchantments != null ? Map.copyOf(enchantments) : Map.of();
    }

    public ItemStack createItemStack() {
        int amount = minAmount;
        if (maxAmount > minAmount) {
            amount += RANDOM.nextInt(maxAmount - minAmount + 1);
        }

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (customName != null && !customName.isBlank()) {
                meta.displayName(MINI_MESSAGE.deserialize(customName));
            }

            if (!lore.isEmpty()) {
                List<Component> loreComponents = new ArrayList<>();
                for (String line : lore) {
                    loreComponents.add(MINI_MESSAGE.deserialize(line));
                }
                meta.lore(loreComponents);
            }

            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public Material getMaterial() { return material; }
    public int getMinAmount() { return minAmount; }
    public int getMaxAmount() { return maxAmount; }
    public double getWeight() { return weight; }
    public String getCustomName() { return customName; }
    public List<String> getLore() { return lore; }
    public Map<Enchantment, Integer> getEnchantments() { return enchantments; }
}
