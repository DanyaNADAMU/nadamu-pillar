package space.nadamu.nadamupillar.disaster;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DisasterContext {
    private final World world;
    private final List<Player> alivePlayers;
    private final Location centerLocation;
    private final Random random;

    public DisasterContext(World world, List<Player> alivePlayers, Location centerLocation, Random random) {
        this.world = world;
        this.alivePlayers = alivePlayers != null ? alivePlayers : Collections.emptyList();
        this.centerLocation = centerLocation;
        this.random = random != null ? random : new Random();
    }

    public World getWorld() {
        return world;
    }

    public List<Player> getAlivePlayers() {
        return Collections.unmodifiableList(alivePlayers);
    }

    public Location getCenterLocation() {
        return centerLocation;
    }

    public Random getRandom() {
        return random;
    }
}
