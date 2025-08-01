package space.nadamu.nadamuPillar;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;

public abstract class Game {

    public static boolean run = false;
    public static int currentPlayers = 0;
    public static HashSet<String> ignorePlayers = new HashSet<>();;

    public static boolean start() {
        if(!run) return false;
        World world = Bukkit.getWorld("world");
        assert world != null;
        world.setPVP(false);
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        currentPlayers = 0;
        for(Player player : onlinePlayers) {
            if(!ignorePlayers.contains(player.getName())) {
                ++currentPlayers;
                player.setGameMode(GameMode.SPECTATOR);
            }
        }

        return true;
    }
}
