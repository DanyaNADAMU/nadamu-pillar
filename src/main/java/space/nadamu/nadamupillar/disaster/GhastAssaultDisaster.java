package space.nadamu.nadamupillar.disaster;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class GhastAssaultDisaster implements Disaster {
    @Override
    public String getId() {
        return "ghast_assault";
    }

    @Override
    public String getDisplayName() {
        return "Атака Гастов";
    }

    @Override
    public String getWarningMessage() {
        return "<red><bold>⚠ ВНИМАНИЕ:</bold> <yellow>Зловещие стоны в вышине... Нападение Гастов!</yellow></red>";
    }

    @Override
    public Set<Environment> getAllowedEnvironments() {
        return EnumSet.allOf(Environment.class);
    }

    @Override
    public void execute(DisasterContext context) {
        World world = context.getWorld();
        if (world == null) return;

        Location center = context.getCenterLocation();
        if (center == null && !context.getAlivePlayers().isEmpty()) {
            center = context.getAlivePlayers().get(0).getLocation();
        }
        if (center == null) return;

        Random random = context.getRandom();
        int ghastCount = 1 + (context.getAlivePlayers().size() >= 4 ? 1 : 0);

        for (int i = 0; i < ghastCount; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double dist = 12.0 + random.nextDouble() * 8.0;
            Location spawnLoc = center.clone().add(
                    Math.cos(angle) * dist,
                    10.0 + random.nextInt(6),
                    Math.sin(angle) * dist
            );

            try {
                world.spawn(spawnLoc, Ghast.class);
            } catch (Exception ignored) {
                // Safe fallback
            }
        }
    }
}
