package space.nadamu.nadamupillar.listener;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import space.nadamu.nadamupillar.fsm.GameManager;

import java.util.Objects;

public class GameProtectionListener implements Listener {
    private final GameManager gameManager;

    public GameProtectionListener(GameManager gameManager) {
        this.gameManager = Objects.requireNonNull(gameManager, "gameManager cannot be null");
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        boolean isPlayerAttacker = false;
        if (event.getDamager() instanceof Player) {
            isPlayerAttacker = true;
        } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player) {
            isPlayerAttacker = true;
        }

        if (isPlayerAttacker && !gameManager.getCurrentState().canPvp()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!gameManager.getCurrentState().canBreakBlocks()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!gameManager.getCurrentState().canBreakBlocks()) {
            event.setCancelled(true);
        }
    }
}
