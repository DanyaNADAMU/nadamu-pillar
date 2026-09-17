package space.nadamu.nadamupillar.arena;

import org.bukkit.Location;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ArenaService {
    List<Location> generateArena(int playerCount);

    Location getSpectatorLocation();

    CompletableFuture<Void> clearArena();
}
