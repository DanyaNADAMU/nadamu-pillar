package space.nadamu.nadamupillar.arena;

import org.bukkit.configuration.file.YamlConfiguration;
import space.nadamu.nadamupillar.arena.model.MapConfig;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

public class MapManager {
    private final File mapsFolder;
    private final Logger logger;
    private final Map<String, MapConfig> maps = new LinkedHashMap<>();
    private final Random random = new Random();
    private volatile MapConfig currentMap;

    public MapManager(File dataFolder, Logger logger) {
        this.mapsFolder = new File(dataFolder, "maps");
        this.logger = logger != null ? logger : Logger.getLogger(MapManager.class.getName());
    }

    public void loadMaps() {
        maps.clear();

        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
        }

        File[] files = mapsFolder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files == null || files.length == 0) {
            try (var stream = getClass().getClassLoader().getResourceAsStream("maps/classic_neon.yml")) {
                if (stream != null) {
                    File target = new File(mapsFolder, "classic_neon.yml");
                    Files.copy(stream, target.toPath());
                }
            } catch (Exception ignored) {
            }
            files = mapsFolder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        }

        if (files == null || files.length == 0) {
            // Create default map file
            MapConfig def = MapConfig.createDefault();
            maps.put(def.getId(), def);
            currentMap = def;
            saveDefaultMapYaml(def);
            logger.info("Created and loaded default map: " + def.getId());
            return;
        }

        for (File file : files) {
            try {
                String id = file.getName().replace(".yml", "").replace(".yaml", "");
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                MapConfig config = MapConfig.fromYaml(id, yaml);
                maps.put(config.getId(), config);
                logger.info("Loaded map configuration: " + config.getId());
            } catch (Exception e) {
                logger.warning("Failed to load map file " + file.getName() + ": " + e.getMessage());
            }
        }

        if (maps.isEmpty()) {
            MapConfig def = MapConfig.createDefault();
            maps.put(def.getId(), def);
        }

        currentMap = maps.values().iterator().next();
    }

    private void saveDefaultMapYaml(MapConfig config) {
        File file = new File(mapsFolder, config.getId() + ".yml");
        if (file.exists()) return;

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("name", config.getDisplayName());
        yaml.set("world", config.getWorldName());
        yaml.set("geometry.y-level", config.getPillarHeight());
        yaml.set("geometry.distance-between-players", config.getDistanceBetweenPlayers());

        yaml.set("pillars.size", config.getPillarSize());
        yaml.set("pillars.depth", config.getPillarDepth());
        yaml.set("pillars.surface", config.getPillarSurfacePattern());
        yaml.set("pillars.body", config.getPillarBodyPattern());
        yaml.set("pillars.bottom", config.getPillarBottomMaterial().name());

        yaml.set("center.enabled", config.isCenterEnabled());
        yaml.set("center.radius", config.getCenterRadius());
        yaml.set("center.y-offset", config.getCenterYOffset());
        yaml.set("center.pattern", config.getCenterPattern());
        yaml.set("center.rim.enabled", config.isCenterRimEnabled());
        yaml.set("center.rim.pattern", config.getCenterRimPattern());

        yaml.set("features.allowed-disasters", config.getAllowedDisasters());

        try {
            yaml.save(file);
        } catch (Exception e) {
            logger.warning("Could not save default map YAML: " + e.getMessage());
        }
    }

    public Optional<MapConfig> getMap(String id) {
        return Optional.ofNullable(maps.get(id.toLowerCase()));
    }

    public Collection<MapConfig> getAllMaps() {
        return Collections.unmodifiableCollection(maps.values());
    }

    public MapConfig getCurrentMap() {
        if (currentMap == null) {
            currentMap = MapConfig.createDefault();
        }
        return currentMap;
    }

    public void setCurrentMap(MapConfig map) {
        if (map != null) {
            this.currentMap = map;
        }
    }

    public MapConfig pickRandomMap() {
        if (maps.isEmpty()) {
            return MapConfig.createDefault();
        }
        List<MapConfig> list = new ArrayList<>(maps.values());
        currentMap = list.get(random.nextInt(list.size()));
        return currentMap;
    }
}
