package space.nadamu.nadamupillar.arena.model;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapConfig {
    private final String id;
    private final String displayName;
    private final String worldName;
    private final int pillarHeight;
    private final double distanceBetweenPlayers;

    private final int pillarSize;
    private final int pillarDepth;
    private final String pillarSurfacePattern;
    private final String pillarBodyPattern;
    private final Material pillarBottomMaterial;

    private final boolean centerEnabled;
    private final double centerRadius;
    private final int centerYOffset;
    private final String centerPattern;
    private final boolean centerRimEnabled;
    private final String centerRimPattern;

    private final List<String> allowedDisasters;

    public MapConfig(
            String id,
            String displayName,
            String worldName,
            int pillarHeight,
            double distanceBetweenPlayers,
            int pillarSize,
            int pillarDepth,
            String pillarSurfacePattern,
            String pillarBodyPattern,
            Material pillarBottomMaterial,
            boolean centerEnabled,
            double centerRadius,
            int centerYOffset,
            String centerPattern,
            boolean centerRimEnabled,
            String centerRimPattern,
            List<String> allowedDisasters
    ) {
        this.id = id;
        this.displayName = displayName != null ? displayName : "<aqua>" + id + "</aqua>";
        this.worldName = worldName != null ? worldName : "world";
        this.pillarHeight = pillarHeight;
        this.distanceBetweenPlayers = distanceBetweenPlayers;
        this.pillarSize = Math.max(1, pillarSize);
        this.pillarDepth = Math.max(1, pillarDepth);
        this.pillarSurfacePattern = pillarSurfacePattern != null ? pillarSurfacePattern : "stone";
        this.pillarBodyPattern = pillarBodyPattern != null ? pillarBodyPattern : "cobblestone";
        this.pillarBottomMaterial = pillarBottomMaterial != null ? pillarBottomMaterial : Material.BEDROCK;
        this.centerEnabled = centerEnabled;
        this.centerRadius = centerRadius;
        this.centerYOffset = centerYOffset;
        this.centerPattern = centerPattern != null ? centerPattern : "cyan_concrete";
        this.centerRimEnabled = centerRimEnabled;
        this.centerRimPattern = centerRimPattern != null ? centerRimPattern : "sea_lantern";
        this.allowedDisasters = allowedDisasters != null ? allowedDisasters : new ArrayList<>();
    }

    public static MapConfig fromYaml(String id, YamlConfiguration yaml) {
        String displayName = yaml.getString("name", id);
        String worldName = yaml.getString("world", "world");

        ConfigurationSection geo = yaml.getConfigurationSection("geometry");
        int pillarHeight = geo != null ? geo.getInt("y-level", 90) : 90;
        double distance = geo != null ? geo.getDouble("distance-between-players", 14.0) : 14.0;

        ConfigurationSection pil = yaml.getConfigurationSection("pillars");
        int pillarSize = pil != null ? pil.getInt("size", 3) : 3;
        int pillarDepth = pil != null ? pil.getInt("depth", 15) : 15;
        String surface = pil != null ? pil.getString("surface", "70%light_gray_concrete,30%cyan_terracotta") : "70%light_gray_concrete,30%cyan_terracotta";
        String body = pil != null ? pil.getString("body", "80%gray_concrete,20%cyan_concrete") : "80%gray_concrete,20%cyan_concrete";
        String bottomStr = pil != null ? pil.getString("bottom", "BEDROCK") : "BEDROCK";
        Material bottomMat = Material.matchMaterial(bottomStr.toUpperCase());
        if (bottomMat == null) bottomMat = Material.BEDROCK;

        ConfigurationSection ctr = yaml.getConfigurationSection("center");
        boolean centerEnabled = ctr == null || ctr.getBoolean("enabled", true);
        double centerRadius = ctr != null ? ctr.getDouble("radius", 5.5) : 5.5;
        int centerYOffset = ctr != null ? ctr.getInt("y-offset", -3) : -3;
        String centerPattern = ctr != null ? ctr.getString("pattern", "60%light_blue_concrete,40%cyan_concrete") : "60%light_blue_concrete,40%cyan_concrete";

        boolean rimEnabled = false;
        String rimPattern = "sea_lantern";
        if (ctr != null && ctr.isConfigurationSection("rim")) {
            ConfigurationSection rim = ctr.getConfigurationSection("rim");
            rimEnabled = rim.getBoolean("enabled", true);
            rimPattern = rim.getString("pattern", "70%sea_lantern,30%cyan_concrete");
        }

        List<String> disasters = yaml.getStringList("features.allowed-disasters");

        return new MapConfig(
                id, displayName, worldName, pillarHeight, distance,
                pillarSize, pillarDepth, surface, body, bottomMat,
                centerEnabled, centerRadius, centerYOffset, centerPattern,
                rimEnabled, rimPattern, disasters
        );
    }

    public static MapConfig createDefault() {
        return new MapConfig(
                "classic_neon",
                "<aqua><bold>Неоновый Октагон</bold></aqua>",
                "world",
                90,
                14.0,
                3,
                15,
                "70%light_gray_concrete,30%cyan_terracotta",
                "80%gray_concrete,20%cyan_concrete",
                Material.BEDROCK,
                true,
                5.5,
                -3,
                "60%light_blue_concrete,40%cyan_concrete",
                true,
                "70%sea_lantern,30%cyan_concrete",
                List.of("meteor_shower", "anvil_rain", "ghast_assault", "wind_charge_storm")
        );
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getWorldName() { return worldName; }
    public int getPillarHeight() { return pillarHeight; }
    public double getDistanceBetweenPlayers() { return distanceBetweenPlayers; }
    public int getPillarSize() { return pillarSize; }
    public int getPillarDepth() { return pillarDepth; }
    public String getPillarSurfacePattern() { return pillarSurfacePattern; }
    public String getPillarBodyPattern() { return pillarBodyPattern; }
    public Material getPillarBottomMaterial() { return pillarBottomMaterial; }
    public boolean isCenterEnabled() { return centerEnabled; }
    public double getCenterRadius() { return centerRadius; }
    public int getCenterYOffset() { return centerYOffset; }
    public String getCenterPattern() { return centerPattern; }
    public boolean isCenterRimEnabled() { return centerRimEnabled; }
    public String getCenterRimPattern() { return centerRimPattern; }
    public List<String> getAllowedDisasters() { return allowedDisasters; }
}
