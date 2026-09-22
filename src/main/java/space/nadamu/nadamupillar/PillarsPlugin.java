package space.nadamu.nadamupillar;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import space.nadamu.nadamupillar.arena.ArenaService;
import space.nadamu.nadamupillar.arena.MapManager;
import space.nadamu.nadamupillar.arena.ProceduralArenaService;
import space.nadamu.nadamupillar.command.PillarsCommand;
import space.nadamu.nadamupillar.disaster.DisasterManager;
import space.nadamu.nadamupillar.fsm.GameManager;
import space.nadamu.nadamupillar.listener.GameProtectionListener;
import space.nadamu.nadamupillar.listener.PlayerConnectionListener;
import space.nadamu.nadamupillar.listener.VoidTrackingListener;
import space.nadamu.nadamupillar.loot.LootService;
import space.nadamu.nadamupillar.registry.PlayerRegistry;

public final class PillarsPlugin extends JavaPlugin {
    private PlayerRegistry playerRegistry;
    private MapManager mapManager;
    private ArenaService arenaService;
    private LootService lootService;
    private DisasterManager disasterManager;
    private GameManager gameManager;
    private BukkitTask tickTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // 1. Dependency Injection setup
        this.playerRegistry = new PlayerRegistry();
        this.mapManager = new MapManager(getDataFolder(), getLogger());
        this.mapManager.loadMaps(getConfig().getString("game.default-map", "classic_bedrock"));
        this.arenaService = new ProceduralArenaService(mapManager);
        this.lootService = new LootService(getDataFolder(), getLogger());
        this.lootService.loadLoot();
        this.disasterManager = new DisasterManager(getLogger());
        this.gameManager = new GameManager(playerRegistry, arenaService, lootService, disasterManager, getLogger());

        this.gameManager.setMinPlayers(getConfig().getInt("game.min-players", 2));
        this.gameManager.setCountdownSeconds(getConfig().getInt("game.countdown-seconds", 5));
        this.gameManager.setCelebrationSeconds(getConfig().getInt("game.celebration-seconds", 7));
        this.gameManager.setLootIntervalSeconds(getConfig().getInt("game.loot-interval-seconds", 5));
        this.gameManager.setDisasterIntervalSeconds(getConfig().getInt("game.disaster-interval-seconds", 30));
        this.gameManager.setAutoStartEnabled(getConfig().getBoolean("game.auto-start", true));

        // 2. Register all listeners once at startup
        double voidDeathY = getConfig().getDouble("world.void-death-y", -70.0);
        getServer().getPluginManager().registerEvents(new VoidTrackingListener(gameManager, playerRegistry, arenaService, voidDeathY), this);
        getServer().getPluginManager().registerEvents(new GameProtectionListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(gameManager, playerRegistry, arenaService), this);

        // 3. Register commands
        PluginCommand command = getCommand("pillars");
        if (command != null) {
            PillarsCommand pillarsCommand = new PillarsCommand(gameManager);
            command.setExecutor(pillarsCommand);
            command.setTabCompleter(pillarsCommand);
        }

        // 4. Check dependencies (FAWE)
        checkDependencies();

        // 5. Start main game tick loop
        this.tickTask = getServer().getScheduler().runTaskTimer(this, gameManager::tick, 1L, 1L);

        getLogger().info("Pillars of Fortune (nadamu-pillar) has been successfully enabled!");
    }

    private void checkDependencies() {
        boolean hasFawe = getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") != null
                || getServer().getPluginManager().getPlugin("WorldEdit") != null;
        if (!hasFawe) {
            getLogger().warning("====================================================");
            getLogger().warning(" FastAsyncWorldEdit (FAWE) is NOT installed!");
            getLogger().warning(" FAWE is required for asynchronous arena generation and resets.");
            getLogger().warning(" Without FAWE, block operations will run on the main thread,");
            getLogger().warning(" which may cause noticeable lag spikes during arena build/reset.");
            getLogger().warning(" Download FAWE: https://intellectualsites.github.io/download/fawe.html");
            getLogger().warning("====================================================");
        } else {
            getLogger().info("FastAsyncWorldEdit (FAWE) integration detected.");
        }
    }

    @Override
    public void onDisable() {
        if (tickTask != null && !tickTask.isCancelled()) {
            tickTask.cancel();
        }

        if (gameManager != null) {
            gameManager.stopMatch();
        }

        if (playerRegistry != null) {
            playerRegistry.clear();
        }

        getLogger().info("Pillars of Fortune (nadamu-pillar) has been disabled.");
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public PlayerRegistry getPlayerRegistry() {
        return playerRegistry;
    }

    public ArenaService getArenaService() {
        return arenaService;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public LootService getLootService() {
        return lootService;
    }

    public DisasterManager getDisasterManager() {
        return disasterManager;
    }
}
