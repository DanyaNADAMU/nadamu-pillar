package space.nadamu.nadamupillar.fsm;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import space.nadamu.nadamupillar.domain.GamePlayer;
import space.nadamu.nadamupillar.domain.PlayerRole;
import space.nadamu.nadamupillar.registry.PlayerRegistry;

import java.util.Objects;

public class WaitingState implements GameState {
    private final GameManager gameManager;
    private final PlayerRegistry playerRegistry;
    private final int minPlayers;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public WaitingState(GameManager gameManager, PlayerRegistry playerRegistry, int minPlayers) {
        this.gameManager = Objects.requireNonNull(gameManager, "gameManager cannot be null");
        this.playerRegistry = Objects.requireNonNull(playerRegistry, "playerRegistry cannot be null");
        this.minPlayers = Math.max(1, minPlayers);
    }

    @Override
    public void onEnter() {
        for (GamePlayer gp : playerRegistry.getAllPlayers()) {
            gp.setRole(PlayerRole.SPECTATOR);
            Player p = gp.getBukkitPlayer();
            if (p != null && p.isOnline()) {
                p.setGameMode(GameMode.SPECTATOR);
                p.sendMessage(MINI_MESSAGE.deserialize("<gray>Ожидание игроков для начала раунда...</gray>"));
            }
        }
    }

    @Override
    public void onTick(int currentTick) {
        // Check if sufficient players are online to start automatically every 20 ticks
        if (currentTick % 20 == 0) {
            if (gameManager.isAutoStartEnabled() && playerRegistry.getTotalCount() >= minPlayers) {
                gameManager.transitionTo(new StartingState(gameManager, playerRegistry, gameManager.getArenaService(), gameManager.getCountdownSeconds(), minPlayers));
            }
        }
    }

    @Override
    public void onExit() {
        // Cleanup if needed
    }

    @Override
    public boolean canPvp() {
        return false;
    }

    @Override
    public boolean canBreakBlocks() {
        return false;
    }

    @Override
    public String getName() {
        return "WAITING";
    }

    public int getMinPlayers() {
        return minPlayers;
    }
}
