package space.nadamu.nadamupillar.arena;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockPatternParser {
    private final List<MaterialEntry> entries = new ArrayList<>();
    private double totalWeight = 0.0;
    private static final Random RANDOM = new Random();

    private record MaterialEntry(Material material, double cumulativeWeight) {}

    public BlockPatternParser(String patternString) {
        if (patternString == null || patternString.isBlank()) {
            addEntry(Material.STONE, 1.0);
            return;
        }

        // Example syntax: "70%light_blue_concrete,30%cyan_concrete" or "stone"
        String[] tokens = patternString.split(",");
        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

            double weight = 1.0;
            String materialName = token;

            int percentIndex = token.indexOf('%');
            if (percentIndex != -1) {
                try {
                    weight = Double.parseDouble(token.substring(0, percentIndex).trim());
                    materialName = token.substring(percentIndex + 1).trim();
                } catch (NumberFormatException ignored) {
                    weight = 1.0;
                }
            }

            Material mat = Material.matchMaterial(materialName.toUpperCase());
            if (mat != null && mat.isBlock()) {
                addEntry(mat, Math.max(0.01, weight));
            }
        }

        if (entries.isEmpty()) {
            addEntry(Material.STONE, 1.0);
        }
    }

    private void addEntry(Material material, double weight) {
        totalWeight += weight;
        entries.add(new MaterialEntry(material, totalWeight));
    }

    public Material sample() {
        if (entries.size() == 1) {
            return entries.get(0).material();
        }

        double r = RANDOM.nextDouble() * totalWeight;
        for (MaterialEntry entry : entries) {
            if (r <= entry.cumulativeWeight()) {
                return entry.material();
            }
        }
        return entries.get(entries.size() - 1).material();
    }
}
