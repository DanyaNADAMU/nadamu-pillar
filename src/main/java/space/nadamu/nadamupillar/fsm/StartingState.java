package space.nadamu.nadamupillar.fsm;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import space.nadamu.nadamupillar.arena.ArenaService;
import space.nadamu.nadamupillar.domain.GamePlayer;
import space.nadamu.nadamupillar.domain.PlayerRole;
import space.nadamu.nadamupillar.registry.PlayerRegistry;

import java.time.Duration;
import java.util.Objects;

public class StartingState implements GameState {
    private final GameManager gameManager;
    private final PlayerRegistry playerRegistry;
    private final ArenaService arenaService;
    private final int countdownSeconds;
    private final int minPlayers;
    private int remainingTicks;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public StartingState(GameManager gameManager, PlayerRegistry playerRegistry, ArenaService arenaService, int countdownSeconds, int minPlayers) {
        this.gameManager = Objects.requireNonNull(gameManager, "gameManager cannot be null");
        this.playerRegistry = Objects.requireNonNull(playerRegistry, "playerRegistry cannot be null");
        this.arenaService = Objects.requireNonNull(arenaService, "arenaService cannot be null");
        this.countdownSeconds = Math.max(3, countdownSeconds);
        this.minPlayers = Math.max(1, minPlayers);
        this.remainingTicks = this.countdownSeconds * 20;
    }

    public StartingState(GameManager gameManager, PlayerRegistry playerRegistry, ArenaService arenaService, int countdownSeconds) {
        this(gameManager, playerRegistry, arenaService, countdownSeconds, 2);
    }

    @Override
    public void onEnter() {
        // Players remain in SPECTATOR mode during the 5s countdown
        for (GamePlayer gp : playerRegistry.getAllPlayers()) {
            Player player = gp.getBukkitPlayer();
            if (player != null && player.isOnline()) {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(MINI_MESSAGE.deserialize("<gold>Начинается обратный отсчет до битвы!</gold>"));
            }
        }
    }

    @Override
    public void onTick(int currentTick) {
        // If someone left and player count dropped below minPlayers, abort countdown
        if (playerRegistry.getTotalCount() < minPlayers) {
            broadcastMessage("<yellow>Отсчет отменен: недостаточно игроков для начала матча.</yellow>");
            gameManager.transitionTo(new WaitingState(gameManager, playerRegistry, minPlayers));
            return;
        }

        if (remainingTicks % 20 == 0) {
            int secondsLeft = remainingTicks / 20;
            if (secondsLeft > 0) {
                broadcastCountdown(secondsLeft);
            }
        }

        remainingTicks--;

        if (remainingTicks <= 0) {
            // Transition to ACTIVE where N pillars will be generated for the remaining players
            gameManager.transitionTo(new ActiveState(gameManager, playerRegistry, arenaService));
        }
    }

    private void broadcastCountdown(int seconds) {
        Title title = Title.title(
                MINI_MESSAGE.deserialize("<gold><bold>" + seconds + "</bold></gold>"),
                MINI_MESSAGE.deserialize("<yellow>Приготовьтесь к битве!</yellow>"),
                Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(800), Duration.ofMillis(200))
        );

        for (GamePlayer gp : playerRegistry.getAllPlayers()) {
            Player player = gp.getBukkitPlayer();
            if (player != null && player.isOnline()) {
                player.showTitle(title);
                player.sendActionBar(MINI_MESSAGE.deserialize("<yellow>Старт через: <gold><bold>" + seconds + " сек.</bold></gold></yellow>"));
            }
        }
    }

    private void broadcastMessage(String miniMessageText) {
        for (GamePlayer gp : playerRegistry.getAllPlayers()) {
            Player player = gp.getBukkitPlayer();
            if (player != null && player.isOnline()) {
                player.sendMessage(MINI_MESSAGE.deserialize(miniMessageText));
            }
        }
    }

    @Override
    public void onExit() {
        // Clear titles
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
        return "STARTING";
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }
}
