package space.nadamu.nadamupillar.scheduler;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import space.nadamu.nadamupillar.arena.ArenaService;
import space.nadamu.nadamupillar.arena.ProceduralArenaService;
import space.nadamu.nadamupillar.disaster.Disaster;
import space.nadamu.nadamupillar.disaster.DisasterContext;
import space.nadamu.nadamupillar.disaster.DisasterManager;
import space.nadamu.nadamupillar.domain.GamePlayer;
import space.nadamu.nadamupillar.domain.PlayerRole;
import space.nadamu.nadamupillar.loot.LootService;
import space.nadamu.nadamupillar.registry.PlayerRegistry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ActionScheduler {
    private final PlayerRegistry playerRegistry;
    private final LootService lootService;
    private final DisasterManager disasterManager;
    private final ArenaService arenaService;

    private final int lootIntervalSeconds;
    private final int disasterIntervalSeconds;

    private int remainingLootTicks;
    private int remainingDisasterTicks;

    private Disaster pendingDisaster;
    private final Random random = new Random();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public ActionScheduler(PlayerRegistry playerRegistry,
                           LootService lootService,
                           DisasterManager disasterManager,
                           ArenaService arenaService,
                           int lootIntervalSeconds,
                           int disasterIntervalSeconds) {
        this.playerRegistry = Objects.requireNonNull(playerRegistry, "playerRegistry cannot be null");
        this.lootService = Objects.requireNonNull(lootService, "lootService cannot be null");
        this.disasterManager = Objects.requireNonNull(disasterManager, "disasterManager cannot be null");
        this.arenaService = Objects.requireNonNull(arenaService, "arenaService cannot be null");
        this.lootIntervalSeconds = Math.max(1, lootIntervalSeconds);
        this.disasterIntervalSeconds = Math.max(5, disasterIntervalSeconds);

        this.remainingLootTicks = this.lootIntervalSeconds * 20;
        this.remainingDisasterTicks = this.disasterIntervalSeconds * 20;
    }

    public ActionScheduler(PlayerRegistry playerRegistry,
                           LootService lootService,
                           DisasterManager disasterManager,
                           ArenaService arenaService) {
        this(playerRegistry, lootService, disasterManager, arenaService, 10, 30);
    }

    public void tick(int currentTick) {
        tickLoot();
        tickDisaster();
        if (currentTick % 5 == 0) {
            updateActionBars();
        }
    }

    private void tickLoot() {
        remainingLootTicks--;
        if (remainingLootTicks <= 0) {
            // Distribute loot to all alive players
            for (GamePlayer gp : playerRegistry.getAlivePlayers()) {
                Player player = gp.getBukkitPlayer();
                if (player != null && player.isOnline()) {
                    lootService.giveRandomLoot(player);
                }
            }
            remainingLootTicks = lootIntervalSeconds * 20;
        }
    }

    private void tickDisaster() {
        remainingDisasterTicks--;

        // Trigger warning at 5 seconds left (100 ticks)
        if (remainingDisasterTicks == 100 || (remainingDisasterTicks < 100 && pendingDisaster == null)) {
            prepareDisasterWarning();
        }

        if (remainingDisasterTicks <= 0) {
            executeDisaster();
            remainingDisasterTicks = disasterIntervalSeconds * 20;
            pendingDisaster = null;
        }
    }

    private void prepareDisasterWarning() {
        World world = getActiveWorld();
        World.Environment env = world != null ? world.getEnvironment() : World.Environment.NORMAL;

        List<String> allowed = Collections.emptyList();
        if (arenaService instanceof ProceduralArenaService pas) {
            allowed = pas.getMapManager().getCurrentMap().getAllowedDisasters();
        }

        pendingDisaster = disasterManager.selectRandomDisaster(env, allowed).orElse(null);
        if (pendingDisaster == null) {
            return;
        }

        Title title = Title.title(
                MINI_MESSAGE.deserialize("<red><bold>⚠ КАТАСТРОФА!</bold></red>"),
                MINI_MESSAGE.deserialize("<yellow>" + pendingDisaster.getDisplayName() + " через 5с!</yellow>"),
                Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(1200), Duration.ofMillis(300))
        );

        for (GamePlayer gp : playerRegistry.getAllPlayers()) {
            Player player = gp.getBukkitPlayer();
            if (player != null && player.isOnline()) {
                player.showTitle(title);
                player.sendMessage(MINI_MESSAGE.deserialize(pendingDisaster.getWarningMessage()));
            }
        }
    }

    private void executeDisaster() {
        if (pendingDisaster == null) {
            return;
        }

        World world = getActiveWorld();
        List<Player> aliveBukkitPlayers = new ArrayList<>();
        for (GamePlayer gp : playerRegistry.getAlivePlayers()) {
            Player p = gp.getBukkitPlayer();
            if (p != null && p.isOnline()) {
                aliveBukkitPlayers.add(p);
            }
        }

        Location centerLoc = arenaService.getSpectatorLocation();
        if (centerLoc != null) {
            centerLoc = centerLoc.clone().subtract(0, 15, 0); // Ground level
        }

        DisasterContext context = new DisasterContext(world, aliveBukkitPlayers, centerLoc, random);
        try {
            pendingDisaster.execute(context);
        } catch (Exception e) {
            Bukkit.getLogger().warning("Error executing disaster " + pendingDisaster.getId() + ": " + e.getMessage());
        }

        // Announcement
        String strikeMsg = "<red><bold>⚡ КАТАСТРОФА:</bold> <gold>" + pendingDisaster.getDisplayName() + "</gold> обрушилась на арену!</red>";
        for (GamePlayer gp : playerRegistry.getAllPlayers()) {
            Player p = gp.getBukkitPlayer();
            if (p != null && p.isOnline()) {
                p.sendMessage(MINI_MESSAGE.deserialize(strikeMsg));
            }
        }
    }

    private void updateActionBars() {
        int lootSec = Math.max(0, (remainingLootTicks + 19) / 20);
        int disasterSec = Math.max(0, (remainingDisasterTicks + 19) / 20);

        String barContent;
        if (pendingDisaster != null && remainingDisasterTicks <= 100) {
            barContent = "<red><bold>⚠ " + pendingDisaster.getDisplayName() + " через " + disasterSec + "с!</bold></red>";
        } else {
            barContent = "<yellow>⚔ Лут: <green><bold>" + String.format("%02d", lootSec) + "s</bold></green> | Катастрофа: <red><bold>" + String.format("%02d", disasterSec) + "s</bold></red></yellow>";
        }

        var component = MINI_MESSAGE.deserialize(barContent);
        for (GamePlayer gp : playerRegistry.getAllPlayers()) {
            Player p = gp.getBukkitPlayer();
            if (p != null && p.isOnline()) {
                p.sendActionBar(component);
            }
        }
    }

    private World getActiveWorld() {
        if (!Bukkit.getWorlds().isEmpty()) {
            return Bukkit.getWorlds().get(0);
        }
        return null;
    }

    public int getRemainingLootTicks() {
        return remainingLootTicks;
    }

    public int getRemainingDisasterTicks() {
        return remainingDisasterTicks;
    }

    public Disaster getPendingDisaster() {
        return pendingDisaster;
    }
}
