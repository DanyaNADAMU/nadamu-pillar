package space.nadamu.nadamupillar.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import space.nadamu.nadamupillar.arena.model.MapConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ProceduralArenaService implements ArenaService {
    private final MapManager mapManager;
    private final Set<Location> modifiedBlocks = new HashSet<>();

    public ProceduralArenaService() {
        this(new MapManager(new File("."), null));
    }

    public ProceduralArenaService(MapManager mapManager) {
        this.mapManager = Objects.requireNonNull(mapManager, "mapManager cannot be null");
    }

    private World getWorld(MapConfig config) {
        World world = Bukkit.getWorld(config.getWorldName());
        if (world == null && !Bukkit.getWorlds().isEmpty()) {
            return Bukkit.getWorlds().get(0);
        }
        return world;
    }

    @Override
    public List<Location> generateArena(int playerCount) {
        MapConfig config = mapManager.getCurrentMap();
        World world = getWorld(config);
        List<Location> spawns = new ArrayList<>();
        if (world == null) {
            return spawns;
        }

        int count = Math.max(2, playerCount);

        // 1. Prepare block pattern parsers
        BlockPatternParser surfaceParser = new BlockPatternParser(config.getPillarSurfacePattern());
        BlockPatternParser bodyParser = new BlockPatternParser(config.getPillarBodyPattern());
        BlockPatternParser centerParser = new BlockPatternParser(config.getCenterPattern());
        BlockPatternParser rimParser = new BlockPatternParser(config.getCenterRimPattern());

        // 2. Dynamic radius calculation based on distance and player count
        double distance = config.getDistanceBetweenPlayers();
        double radius = count <= 2
                ? distance / 2.0
                : distance / (2.0 * Math.sin(Math.PI / count));
        radius = Math.clamp(radius, 7.0, 40.0);

        // 3. Generate CubeCraft center platform (if enabled)
        if (config.isCenterEnabled()) {
            int centerY = config.getPillarHeight() + config.getCenterYOffset();
            int centerR = (int) Math.round(config.getCenterRadius());

            for (int x = -centerR; x <= centerR; x++) {
                for (int z = -centerR; z <= centerR; z++) {
                    double dist = Math.sqrt(x * x + z * z);
                    if (dist <= config.getCenterRadius()) {
                        Material mat;
                        if (config.isCenterRimEnabled() && dist >= config.getCenterRadius() - 0.9) {
                            mat = rimParser.sample();
                        } else {
                            mat = centerParser.sample();
                        }

                        Location loc = new Location(world, x, centerY, z);
                        loc.getBlock().setType(mat, false);
                        modifiedBlocks.add(loc);
                    }
                }
            }
        }

        // 4. Generate N pillars equidistant along the perimeter
        int halfSize = config.getPillarSize() / 2;
        int height = config.getPillarHeight();
        int depth = config.getPillarDepth();

        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            double px = Math.round(Math.cos(angle) * radius);
            double pz = Math.round(Math.sin(angle) * radius);

            // Construct platform (e.g. 3x3)
            for (int dx = -halfSize; dx <= halfSize; dx++) {
                for (int dz = -halfSize; dz <= halfSize; dz++) {
                    int bx = (int) px + dx;
                    int bz = (int) pz + dz;

                    // Surface block
                    Location topLoc = new Location(world, bx, height, bz);
                    topLoc.getBlock().setType(surfaceParser.sample(), false);
                    modifiedBlocks.add(topLoc);

                    // Body blocks downwards
                    for (int y = height - 1; y >= height - depth; y--) {
                        Material mat = (y == height - depth)
                                ? config.getPillarBottomMaterial()
                                : bodyParser.sample();
                        Location bodyLoc = new Location(world, bx, y, bz);
                        bodyLoc.getBlock().setType(mat, false);
                        modifiedBlocks.add(bodyLoc);
                    }
                }
            }

            // Calculate inward-facing yaw
            float yaw = (float) Math.toDegrees(Math.atan2(-px, pz));
            Location spawn = new Location(world, px + 0.5, height + 1.0, pz + 0.5, yaw, 0f);
            spawns.add(spawn);
        }

        return spawns;
    }

    @Override
    public Location getSpectatorLocation() {
        MapConfig config = mapManager.getCurrentMap();
        World world = getWorld(config);
        if (world == null) {
            return new Location(null, 0.5, config.getPillarHeight() + 15, 0.5);
        }
        return new Location(world, 0.5, config.getPillarHeight() + 15, 0.5);
    }

    @Override
    public CompletableFuture<Void> clearArena() {
        MapConfig config = mapManager.getCurrentMap();
        World world = getWorld(config);
        if (world != null) {
            // Remove non-player entities safely
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            }

            // Clear modified blocks
            for (Location loc : modifiedBlocks) {
                loc.getBlock().setType(Material.AIR, false);
            }
            modifiedBlocks.clear();
        }

        return CompletableFuture.completedFuture(null);
    }

    public MapManager getMapManager() {
        return mapManager;
    }
}
