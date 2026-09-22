package space.nadamu.nadamupillar.fsm;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import space.nadamu.nadamupillar.arena.ArenaService;
import space.nadamu.nadamupillar.disaster.DisasterManager;
import space.nadamu.nadamupillar.domain.GamePlayer;
import space.nadamu.nadamupillar.domain.PlayerRole;
import space.nadamu.nadamupillar.loot.LootService;
import space.nadamu.nadamupillar.registry.PlayerRegistry;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class GameManager {
    private final PlayerRegistry playerRegistry;
    private final ArenaService arenaService;
    private final LootService lootService;
    private final DisasterManager disasterManager;
    private final Logger logger;
    private final AtomicInteger tickCounter = new AtomicInteger(0);
    private volatile GameState currentState;

    private volatile boolean autoStartEnabled = true;
    private volatile int minPlayers = 2;
    private volatile int countdownSeconds = 5;
    private volatile int celebrationSeconds = 7;
    private volatile int lootIntervalSeconds = 5;
    private volatile int disasterIntervalSeconds = 30;

    public GameManager(PlayerRegistry playerRegistry,
                       ArenaService arenaService,
                       LootService lootService,
                       DisasterManager disasterManager,
                       Logger logger) {
        this.playerRegistry = Objects.requireNonNull(playerRegistry, "playerRegistry cannot be null");
        this.arenaService = Objects.requireNonNull(arenaService, "arenaService cannot be null");
        this.logger = logger != null ? logger : Logger.getLogger(GameManager.class.getName());
        this.lootService = lootService != null ? lootService : new LootService(new File("."), this.logger);
        this.disasterManager = disasterManager != null ? disasterManager : new DisasterManager(this.logger);
        this.currentState = new WaitingState(this, playerRegistry, minPlayers);
    }

    public GameManager(PlayerRegistry playerRegistry, ArenaService arenaService, Logger logger) {
        this(playerRegistry, arenaService, null, null, logger);
    }

    public synchronized void transitionTo(GameState newState) {
        Objects.requireNonNull(newState, "newState cannot be null");
        if (currentState != null) {
            logger.info("Transitioning FSM state: " + currentState.getName() + " -> " + newState.getName());
            currentState.onExit();
        }
        currentState = newState;
        currentState.onEnter();
    }

    public void tick() {
        int current = tickCounter.incrementAndGet();
        GameState state = currentState;
        if (state != null) {
            state.onTick(current);
        }
    }

    public void startMatch(int countdown) {
        this.autoStartEnabled = true;
        transitionTo(new StartingState(this, playerRegistry, arenaService, countdown, minPlayers));
    }

    public void stopMatch() {
        this.autoStartEnabled = false;
        arenaService.clearArena();
        playerRegistry.setAllRoles(PlayerRole.SPECTATOR);
        for (GamePlayer gp : playerRegistry.getAllPlayers()) {
            Player p = gp.getBukkitPlayer();
            if (p != null && p.isOnline()) {
                p.setGameMode(GameMode.SPECTATOR);
                p.getInventory().clear();
            }
        }
        transitionTo(new WaitingState(this, playerRegistry, minPlayers));
    }

    public boolean isAutoStartEnabled() {
        return autoStartEnabled;
    }

    public void setAutoStartEnabled(boolean autoStartEnabled) {
        this.autoStartEnabled = autoStartEnabled;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = Math.max(1, minPlayers);
    }

    public int getCountdownSeconds() {
        return countdownSeconds;
    }

    public void setCountdownSeconds(int countdownSeconds) {
        this.countdownSeconds = Math.max(1, countdownSeconds);
    }

    public int getCelebrationSeconds() {
        return celebrationSeconds;
    }

    public void setCelebrationSeconds(int celebrationSeconds) {
        this.celebrationSeconds = Math.max(1, celebrationSeconds);
    }

    public int getLootIntervalSeconds() {
        return lootIntervalSeconds;
    }

    public void setLootIntervalSeconds(int lootIntervalSeconds) {
        this.lootIntervalSeconds = Math.max(1, lootIntervalSeconds);
    }

    public int getDisasterIntervalSeconds() {
        return disasterIntervalSeconds;
    }

    public void setDisasterIntervalSeconds(int disasterIntervalSeconds) {
        this.disasterIntervalSeconds = Math.max(1, disasterIntervalSeconds);
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public ArenaService getArenaService() {
        return arenaService;
    }

    public PlayerRegistry getPlayerRegistry() {
        return playerRegistry;
    }

    public LootService getLootService() {
        return lootService;
    }

    public DisasterManager getDisasterManager() {
        return disasterManager;
    }
}
