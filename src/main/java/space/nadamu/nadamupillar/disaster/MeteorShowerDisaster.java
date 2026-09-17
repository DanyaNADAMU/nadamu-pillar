package space.nadamu.nadamupillar.disaster;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MeteorShowerDisaster implements Disaster {
    @Override
    public String getId() {
        return "meteor_shower";
    }

    @Override
    public String getDisplayName() {
        return "Метеоритный дождь";
    }

    @Override
    public String getWarningMessage() {
        return "<red><bold>⚠ ВНИМАНИЕ:</bold> <yellow>Метеоритный дождь обрушится на арену через 5 секунд!</yellow></red>";
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

        // Spawn falling TNT meteors above alive players and around arena center
        int meteorCount = Math.max(3, players.size() * 2);

        for (int i = 0; i < meteorCount; i++) {
            Location targetLoc;
            if (!players.isEmpty() && i < players.size()) {
                targetLoc = players.get(i).getLocation().clone();
            } else if (context.getCenterLocation() != null) {
                targetLoc = context.getCenterLocation().clone();
                double offsetX = (random.nextDouble() - 0.5) * 20.0;
                double offsetZ = (random.nextDouble() - 0.5) * 20.0;
                targetLoc.add(offsetX, 0, offsetZ);
            } else {
                continue;
            }

            Location spawnLoc = targetLoc.add(
                    (random.nextDouble() - 0.5) * 4.0,
                    15.0 + random.nextInt(6),
                    (random.nextDouble() - 0.5) * 4.0
            );

            try {
                TNTPrimed tnt = world.spawn(spawnLoc, TNTPrimed.class);
                tnt.setFuseTicks(40 + random.nextInt(30));
                double vx = (random.nextDouble() - 0.5) * 0.4;
                double vy = -0.6 - (random.nextDouble() * 0.3);
                double vz = (random.nextDouble() - 0.5) * 0.4;
                tnt.setVelocity(new Vector(vx, vy, vz));
            } catch (Exception ignored) {
                // MockBukkit or entity limitation fallback
            }
        }
    }
}
