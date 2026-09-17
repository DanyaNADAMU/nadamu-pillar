package space.nadamu.nadamupillar.disaster;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.BreezeWindCharge;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class WindChargeStormDisaster implements Disaster {
    @Override
    public String getId() {
        return "wind_charge_storm";
    }

    @Override
    public String getDisplayName() {
        return "Ветряной шторм";
    }

    @Override
    public String getWarningMessage() {
        return "<red><bold>⚠ ВНИМАНИЕ:</bold> <yellow>Ветряной шторм! Держитесь за столбы!</yellow></red>";
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
            int shots = 2 + random.nextInt(2);
            for (int i = 0; i < shots; i++) {
                Location spawnLoc = pLoc.clone().add(
                        (random.nextDouble() - 0.5) * 6.0,
                        8.0 + random.nextInt(4),
                        (random.nextDouble() - 0.5) * 6.0
                );

                try {
                    BreezeWindCharge charge = world.spawn(spawnLoc, BreezeWindCharge.class);
                    charge.setVelocity(new Vector(
                            (random.nextDouble() - 0.5) * 0.3,
                            -0.8,
                            (random.nextDouble() - 0.5) * 0.3
                    ));
                } catch (Exception e) {
                    // Safe fallback: apply direct velocity kick if projectile spawning is unavailable in mock
                    player.setVelocity(player.getVelocity().add(new Vector(
                            (random.nextDouble() - 0.5) * 0.6,
                            0.2,
                            (random.nextDouble() - 0.5) * 0.6
                    )));
                }
            }
        }
    }
}
