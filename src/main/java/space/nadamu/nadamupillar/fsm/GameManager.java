package space.nadamu.nadamupillar.fsm;

import space.nadamu.nadamupillar.arena.ArenaService;
import space.nadamu.nadamupillar.disaster.DisasterManager;
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
        this.currentState = new WaitingState(this, playerRegistry, 2);
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

    public void startMatch(int countdownSeconds) {
        transitionTo(new StartingState(this, playerRegistry, arenaService, countdownSeconds));
    }

    public void stopMatch() {
        transitionTo(new WaitingState(this, playerRegistry, 2));
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
