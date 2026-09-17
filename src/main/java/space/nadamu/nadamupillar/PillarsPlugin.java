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
        this.mapManager.loadMaps();
        this.arenaService = new ProceduralArenaService(mapManager);
        this.lootService = new LootService(getDataFolder(), getLogger());
        this.lootService.loadLoot();
        this.disasterManager = new DisasterManager(getLogger());
        this.gameManager = new GameManager(playerRegistry, arenaService, lootService, disasterManager, getLogger());

        // 2. Register all listeners once at startup
        getServer().getPluginManager().registerEvents(new VoidTrackingListener(gameManager, playerRegistry, arenaService), this);
        getServer().getPluginManager().registerEvents(new GameProtectionListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(gameManager, playerRegistry, arenaService), this);

        // 3. Register commands
        PluginCommand command = getCommand("pillars");
        if (command != null) {
            PillarsCommand pillarsCommand = new PillarsCommand(gameManager);
            command.setExecutor(pillarsCommand);
            command.setTabCompleter(pillarsCommand);
        }

        // 4. Start main game tick loop
        this.tickTask = getServer().getScheduler().runTaskTimer(this, gameManager::tick, 1L, 1L);

        getLogger().info("Pillars of Fortune (nadamu-pillar) has been successfully enabled!");
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
