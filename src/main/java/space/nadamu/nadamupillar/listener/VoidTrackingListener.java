package space.nadamu.nadamupillar.listener;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import space.nadamu.nadamupillar.api.PlayerEliminateEvent;
import space.nadamu.nadamupillar.arena.ArenaService;
import space.nadamu.nadamupillar.domain.GamePlayer;
import space.nadamu.nadamupillar.domain.PlayerRole;
import space.nadamu.nadamupillar.fsm.ActiveState;
import space.nadamu.nadamupillar.fsm.GameManager;
import space.nadamu.nadamupillar.registry.PlayerRegistry;

import java.util.Objects;
import java.util.Optional;

public class VoidTrackingListener implements Listener {
    private final GameManager gameManager;
    private final PlayerRegistry playerRegistry;
    private final ArenaService arenaService;
    private final double voidThreshold;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public VoidTrackingListener(GameManager gameManager, PlayerRegistry playerRegistry, ArenaService arenaService, double voidThreshold) {
        this.gameManager = Objects.requireNonNull(gameManager, "gameManager cannot be null");
        this.playerRegistry = Objects.requireNonNull(playerRegistry, "playerRegistry cannot be null");
        this.arenaService = Objects.requireNonNull(arenaService, "arenaService cannot be null");
        this.voidThreshold = voidThreshold;
    }

    public VoidTrackingListener(GameManager gameManager, PlayerRegistry playerRegistry, ArenaService arenaService) {
        this(gameManager, playerRegistry, arenaService, -70.0);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Optional<GamePlayer> gamePlayerOpt = playerRegistry.getPlayer(player);
        if (gamePlayerOpt.isEmpty() || !gamePlayerOpt.get().isAlive()) {
            return;
        }

        // Intercept fatal damage to prevent vanilla death screen
        if (player.getHealth() - event.getFinalDamage() <= 0.0) {
            event.setCancelled(true);
            eliminatePlayer(player, gamePlayerOpt.get(), event.getCause());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getTo().getY() >= voidThreshold) {
            return;
        }

        Optional<GamePlayer> gamePlayerOpt = playerRegistry.getPlayer(player);
        if (gamePlayerOpt.isEmpty() || !gamePlayerOpt.get().isAlive()) {
            return;
        }

        eliminatePlayer(player, gamePlayerOpt.get(), DamageCause.VOID);
    }

    public void eliminatePlayer(Player player, GamePlayer gamePlayer, DamageCause cause) {
        if (!gamePlayer.isAlive()) {
            return;
        }

        // 1. Reset physical state
        player.setVelocity(new Vector(0, 0, 0));
        player.setFallDistance(0);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.getInventory().clear();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        // 2. Mark as spectator
        gamePlayer.setRole(PlayerRole.SPECTATOR);
        player.setGameMode(GameMode.SPECTATOR);

        // 3. Teleport to spectator vantage point asynchronously
        player.teleportAsync(arenaService.getSpectatorLocation());

        // 4. Notify all players
        for (GamePlayer gp : playerRegistry.getAllPlayers()) {
            Player p = gp.getBukkitPlayer();
            if (p != null && p.isOnline()) {
                p.sendMessage(MINI_MESSAGE.deserialize(
                        "<red><bold>☠</bold> <gray>Игрок <white>" + player.getName() + "</white> выбыл из битвы!</gray></red>"
                ));
            }
        }

        // 5. Fire Bukkit event
        Bukkit.getPluginManager().callEvent(new PlayerEliminateEvent(gamePlayer, cause));

        // 6. Check ending condition if currently in ActiveState
        if (gameManager.getCurrentState() instanceof ActiveState activeState) {
            activeState.checkEndCondition();
        }
    }
}
