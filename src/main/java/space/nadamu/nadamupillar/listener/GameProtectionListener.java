package space.nadamu.nadamupillar.listener;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.world.StructureGrowEvent;
import space.nadamu.nadamupillar.fsm.ActiveState;
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
            return;
        }

        // Track placed block for arena reset
        gameManager.getArenaService().trackBlock(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!gameManager.getCurrentState().canBreakBlocks()) {
            event.setCancelled(true);
            return;
        }

        // Track emptied liquid block (water/lava)
        gameManager.getArenaService().trackBlock(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLiquidFlow(BlockFromToEvent event) {
        if (gameManager.getCurrentState() instanceof ActiveState) {
            gameManager.getArenaService().trackBlock(event.getToBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (gameManager.getCurrentState() instanceof ActiveState) {
            gameManager.getArenaService().trackBlock(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        if (gameManager.getCurrentState() instanceof ActiveState) {
            gameManager.getArenaService().trackBlock(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (gameManager.getCurrentState() instanceof ActiveState) {
            gameManager.getArenaService().trackBlock(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (gameManager.getCurrentState() instanceof ActiveState) {
            gameManager.getArenaService().trackBlock(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (gameManager.getCurrentState() instanceof ActiveState) {
            for (var blockState : event.getBlocks()) {
                gameManager.getArenaService().trackBlock(blockState.getLocation());
            }
        }
    }
}
