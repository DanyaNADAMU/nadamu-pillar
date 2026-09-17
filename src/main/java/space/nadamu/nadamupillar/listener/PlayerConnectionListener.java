package space.nadamu.nadamupillar.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import space.nadamu.nadamupillar.arena.ArenaService;
import space.nadamu.nadamupillar.domain.GamePlayer;
import space.nadamu.nadamupillar.domain.PlayerRole;
import space.nadamu.nadamupillar.fsm.ActiveState;
import space.nadamu.nadamupillar.fsm.GameManager;
import space.nadamu.nadamupillar.fsm.WaitingState;
import space.nadamu.nadamupillar.registry.PlayerRegistry;

import java.util.Objects;
import java.util.Optional;

public class PlayerConnectionListener implements Listener {
    private final GameManager gameManager;
    private final PlayerRegistry playerRegistry;
    private final ArenaService arenaService;

    public PlayerConnectionListener(GameManager gameManager, PlayerRegistry playerRegistry, ArenaService arenaService) {
        this.gameManager = Objects.requireNonNull(gameManager, "gameManager cannot be null");
        this.playerRegistry = Objects.requireNonNull(playerRegistry, "playerRegistry cannot be null");
        this.arenaService = Objects.requireNonNull(arenaService, "arenaService cannot be null");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (gameManager.getCurrentState() instanceof WaitingState) {
            playerRegistry.register(player, PlayerRole.SPECTATOR);
            player.setGameMode(GameMode.SPECTATOR);
            player.teleportAsync(arenaService.getSpectatorLocation());
        } else {
            // Match is in progress
            playerRegistry.register(player, PlayerRole.SPECTATOR);
            player.setGameMode(GameMode.SPECTATOR);
            player.teleportAsync(arenaService.getSpectatorLocation());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Optional<GamePlayer> gamePlayerOpt = playerRegistry.getPlayer(player);

        if (gamePlayerOpt.isPresent()) {
            GamePlayer gp = gamePlayerOpt.get();
            if (gp.isAlive()) {
                gp.setRole(PlayerRole.SPECTATOR);
                if (gameManager.getCurrentState() instanceof ActiveState activeState) {
                    activeState.checkEndCondition();
                }
            }
            playerRegistry.unregister(player.getUniqueId());
        }
    }
}
