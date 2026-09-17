package space.nadamu.nadamupillar.fsm;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import space.nadamu.nadamupillar.arena.ArenaService;
import space.nadamu.nadamupillar.domain.GamePlayer;
import space.nadamu.nadamupillar.domain.PlayerRole;
import space.nadamu.nadamupillar.registry.PlayerRegistry;

import java.time.Duration;
import java.util.Objects;

public class EndingState implements GameState {
    private final GameManager gameManager;
    private final PlayerRegistry playerRegistry;
    private final ArenaService arenaService;
    private final GamePlayer winner;
    private final int celebrationDurationSeconds;
    private int remainingTicks;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public EndingState(GameManager gameManager, PlayerRegistry playerRegistry, ArenaService arenaService, GamePlayer winner, int celebrationDurationSeconds) {
        this.gameManager = Objects.requireNonNull(gameManager, "gameManager cannot be null");
        this.playerRegistry = Objects.requireNonNull(playerRegistry, "playerRegistry cannot be null");
        this.arenaService = Objects.requireNonNull(arenaService, "arenaService cannot be null");
        this.winner = winner;
        this.celebrationDurationSeconds = Math.max(1, celebrationDurationSeconds);
        this.remainingTicks = this.celebrationDurationSeconds * 20;
    }

    @Override
    public void onEnter() {
        Title title;
        if (winner != null) {
            title = Title.title(
                    MINI_MESSAGE.deserialize("<gold><bold>ПОБЕДА!</bold></gold>"),
                    MINI_MESSAGE.deserialize("<yellow>Победитель: <white>" + winner.getName() + "</white>!</yellow>"),
                    Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(4), Duration.ofMillis(500))
            );

            // Let the winner celebrate on their pillar with glowing effect and invulnerability
            Player winnerPlayer = winner.getBukkitPlayer();
            if (winnerPlayer != null && winnerPlayer.isOnline()) {
                winnerPlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, remainingTicks, 1, false, false));
                winnerPlayer.setInvulnerable(true);
            }
        } else {
            title = Title.title(
                    MINI_MESSAGE.deserialize("<gray><bold>НИЧЬЯ!</bold></gray>"),
                    MINI_MESSAGE.deserialize("<yellow>Никто не выжил в этой битве!</yellow>"),
                    Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(4), Duration.ofMillis(500))
            );
        }

        for (GamePlayer gp : playerRegistry.getAllPlayers()) {
            Player player = gp.getBukkitPlayer();
            if (player != null && player.isOnline()) {
                player.showTitle(title);
                if (winner != null) {
                    player.sendMessage(MINI_MESSAGE.deserialize("<gold>★ Победитель раунда: <bold>" + winner.getName() + "</bold>!</gold>"));
                } else {
                    player.sendMessage(MINI_MESSAGE.deserialize("<gray>Раунд окончен вничью.</gray>"));
                }

                if (winner == null || !gp.equals(winner)) {
                    player.setGameMode(GameMode.SPECTATOR);
                }
            }
        }
    }

    @Override
    public void onTick(int currentTick) {
        remainingTicks--;
        if (remainingTicks <= 0) {
            // Clean up arena and return to WAITING
            if (winner != null) {
                Player winnerPlayer = winner.getBukkitPlayer();
                if (winnerPlayer != null && winnerPlayer.isOnline()) {
                    winnerPlayer.setInvulnerable(false);
                    winnerPlayer.removePotionEffect(PotionEffectType.GLOWING);
                    winnerPlayer.setGameMode(GameMode.SPECTATOR);
                }
            }

            arenaService.clearArena();
            playerRegistry.setAllRoles(PlayerRole.SPECTATOR);
            gameManager.transitionTo(new WaitingState(gameManager, playerRegistry, 2));
        }
    }

    @Override
    public void onExit() {
        if (winner != null) {
            Player winnerPlayer = winner.getBukkitPlayer();
            if (winnerPlayer != null && winnerPlayer.isOnline()) {
                winnerPlayer.setInvulnerable(false);
            }
        }
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
        return "ENDING";
    }

    public GamePlayer getWinner() {
        return winner;
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }
}
