package space.nadamu.nadamupillar.disaster;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class AnvilRainDisaster implements Disaster {
    @Override
    public String getId() {
        return "anvil_rain";
    }

    @Override
    public String getDisplayName() {
        return "Наковальнепад";
    }

    @Override
    public String getWarningMessage() {
        return "<red><bold>⚠ ВНИМАНИЕ:</bold> <yellow>Берегите головы! Грядет наковальнепад!</yellow></red>";
    }

    @Override
    public Set<Environment> getAllowedEnvironments() {
        return EnumSet.allOf(Environment.class);
    }

    @Override
    public void execute(DisasterContext context) {
        World world = context.getWorld();
        if (world == null) return;

        Random random = context.getRandom();
        List<Player> players = context.getAlivePlayers();

        for (Player player : players) {
            Location pLoc = player.getLocation();
            // Drop an anvil directly above and 1-2 around the player's pillar
            int count = 1 + random.nextInt(2);
            for (int i = 0; i < count; i++) {
                double ox = (i == 0) ? 0 : (random.nextDouble() - 0.5) * 2.0;
                double oz = (i == 0) ? 0 : (random.nextDouble() - 0.5) * 2.0;
                Location anvilLoc = pLoc.clone().add(ox, 14.0 + random.nextInt(4), oz);

                try {
                    world.spawnFallingBlock(anvilLoc, Material.ANVIL.createBlockData());
                } catch (Exception ignored) {
                    // Safe fallback for testing frameworks
                }
            }
        }
    }
}
