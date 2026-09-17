package space.nadamu.nadamupillar.fsm;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import space.nadamu.nadamupillar.arena.ArenaService;
import space.nadamu.nadamupillar.disaster.DisasterManager;
import space.nadamu.nadamupillar.domain.GamePlayer;
import space.nadamu.nadamupillar.domain.PlayerRole;
import space.nadamu.nadamupillar.loot.LootService;
import space.nadamu.nadamupillar.registry.PlayerRegistry;
import space.nadamu.nadamupillar.scheduler.ActionScheduler;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ActiveState implements GameState {
    private final GameManager gameManager;
    private final PlayerRegistry playerRegistry;
    private final ArenaService arenaService;
    private final LootService lootService;
    private final DisasterManager disasterManager;
    private ActionScheduler actionScheduler;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public ActiveState(GameManager gameManager,
                       PlayerRegistry playerRegistry,
                       ArenaService arenaService,
                       LootService lootService,
                       DisasterManager disasterManager) {
        this.gameManager = Objects.requireNonNull(gameManager, "gameManager cannot be null");
        this.playerRegistry = Objects.requireNonNull(playerRegistry, "playerRegistry cannot be null");
        this.arenaService = Objects.requireNonNull(arenaService, "arenaService cannot be null");
        this.lootService = lootService != null ? lootService : new LootService(new File("."), null);
        this.disasterManager = disasterManager != null ? disasterManager : new DisasterManager();
    }

    public ActiveState(GameManager gameManager, PlayerRegistry playerRegistry, ArenaService arenaService) {
        this(gameManager, playerRegistry, arenaService,
                gameManager != null ? gameManager.getLootService() : null,
                gameManager != null ? gameManager.getDisasterManager() : null);
    }

    @Override
    public void onEnter() {
        List<GamePlayer> readyPlayers = new ArrayList<>();
        for (GamePlayer gp : playerRegistry.getAllPlayers()) {
            Player p = gp.getBukkitPlayer();
            if (p != null && p.isOnline()) {
                readyPlayers.add(gp);
            }
        }

        int count = readyPlayers.size();

        // 1. Procedurally generate N pillars for the exact number of players
        List<Location> spawns = arenaService.generateArena(count);

        // 2. Teleport each player to their pillar and initialize state
        for (int i = 0; i < count; i++) {
            GamePlayer gp = readyPlayers.get(i);
            Player player = gp.getBukkitPlayer();
            if (player == null || !player.isOnline()) {
                continue;
            }

            gp.setRole(PlayerRole.ALIVE);
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.getInventory().clear();
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            if (i < spawns.size()) {
                player.teleportAsync(spawns.get(i));
            }
        }

        // 3. Broadcast start title and message
        Title title = Title.title(
                MINI_MESSAGE.deserialize("<red><bold>В БОЙ!</bold></red>"),
                MINI_MESSAGE.deserialize("<yellow>Выживите любой ценой!</yellow>"),
                Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(1200), Duration.ofMillis(300))
        );

        for (GamePlayer gp : playerRegistry.getAllPlayers()) {
            Player player = gp.getBukkitPlayer();
            if (player != null && player.isOnline()) {
                player.showTitle(title);
                player.sendMessage(MINI_MESSAGE.deserialize("<gold><bold>=== Битва на Столбах Удачи началась! ===</bold></gold>"));
            }
        }

        // 4. Initialize action scheduler for loot and disaster timers
        this.actionScheduler = new ActionScheduler(playerRegistry, lootService, disasterManager, arenaService);

        checkEndCondition();
    }

    @Override
    public void onTick(int currentTick) {
        if (actionScheduler != null) {
            actionScheduler.tick(currentTick);
        }

        if (currentTick % 10 == 0) {
            checkEndCondition();
        }
    }

    public ActionScheduler getActionScheduler() {
        return actionScheduler;
    }

    public void checkEndCondition() {
        int aliveCount = playerRegistry.getAliveCount();
        if (aliveCount <= 1) {
            GamePlayer winner = null;
            Collection<GamePlayer> alivePlayers = playerRegistry.getAlivePlayers();
            if (!alivePlayers.isEmpty()) {
                winner = alivePlayers.iterator().next();
            }
            gameManager.transitionTo(new EndingState(gameManager, playerRegistry, arenaService, winner, 7));
        }
    }

    @Override
    public void onExit() {
    }

    @Override
    public boolean canPvp() {
        return true;
    }

    @Override
    public boolean canBreakBlocks() {
        return true;
    }

    @Override
    public String getName() {
        return "ACTIVE";
    }
}
