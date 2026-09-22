package space.nadamu.nadamupillar;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import space.nadamu.nadamupillar.arena.ProceduralArenaService;
import space.nadamu.nadamupillar.domain.PlayerRole;
import space.nadamu.nadamupillar.fsm.ActiveState;
import space.nadamu.nadamupillar.fsm.GameManager;
import space.nadamu.nadamupillar.listener.GameProtectionListener;
import space.nadamu.nadamupillar.registry.PlayerRegistry;

import static org.junit.jupiter.api.Assertions.*;

class GameProtectionListenerTest {
    private ServerMock server;
    private PlayerRegistry playerRegistry;
    private ProceduralArenaService arenaService;
    private GameManager gameManager;
    private GameProtectionListener listener;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        playerRegistry = new PlayerRegistry();
        arenaService = new ProceduralArenaService();
        gameManager = new GameManager(playerRegistry, arenaService, null);
        listener = new GameProtectionListener(gameManager);
        server.getPluginManager().registerEvents(listener, MockBukkit.createMockPlugin());
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("PvP is blocked in WAITING state")
    void testPvpBlockedInWaiting() {
        Player attacker = server.addPlayer("Attacker");
        Player defender = server.addPlayer("Defender");
        playerRegistry.register(attacker, PlayerRole.ALIVE);
        playerRegistry.register(defender, PlayerRole.ALIVE);

        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(
                attacker, defender, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 5.0
        );
        server.getPluginManager().callEvent(event);

        assertTrue(event.isCancelled(), "PvP should be cancelled in WAITING state");
    }

    @Test
    @DisplayName("PvP is allowed in ACTIVE state with alive participants")
    void testPvpAllowedInActive() {
        Player attacker = server.addPlayer("Attacker");
        Player defender = server.addPlayer("Defender");
        playerRegistry.register(attacker, PlayerRole.ALIVE);
        playerRegistry.register(defender, PlayerRole.ALIVE);

        gameManager.transitionTo(new ActiveState(gameManager, playerRegistry, arenaService));
        assertEquals("ACTIVE", gameManager.getCurrentState().getName());

        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(
                attacker, defender, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 5.0
        );
        server.getPluginManager().callEvent(event);

        assertFalse(event.isCancelled(), "PvP should be allowed in ACTIVE state");
    }

    @Test
    @DisplayName("Block breaking is blocked in WAITING state")
    void testBlockBreakBlockedInWaiting() {
        Player player = server.addPlayer("Builder");
        Block block = player.getWorld().getBlockAt(0, 64, 0);
        block.setType(Material.STONE);

        BlockBreakEvent event = new BlockBreakEvent(block, player);
        server.getPluginManager().callEvent(event);

        assertTrue(event.isCancelled(), "Block break should be cancelled in WAITING state");
    }

    @Test
    @DisplayName("Block breaking is allowed in ACTIVE state")
    void testBlockBreakAllowedInActive() {
        Player p1 = server.addPlayer("Builder1");
        Player p2 = server.addPlayer("Builder2");
        playerRegistry.register(p1, PlayerRole.ALIVE);
        playerRegistry.register(p2, PlayerRole.ALIVE);

        Block block = p1.getWorld().getBlockAt(0, 64, 0);
        block.setType(Material.STONE);

        gameManager.transitionTo(new ActiveState(gameManager, playerRegistry, arenaService));
        assertEquals("ACTIVE", gameManager.getCurrentState().getName());

        BlockBreakEvent event = new BlockBreakEvent(block, p1);
        server.getPluginManager().callEvent(event);

        assertFalse(event.isCancelled(), "Block break should be allowed in ACTIVE state");
    }

    @Test
    @DisplayName("Placed blocks during ACTIVE state are tracked and cleared on arena reset")
    void testPlacedBlocksTrackedAndCleared() {
        Player p1 = server.addPlayer("Builder1");
        Player p2 = server.addPlayer("Builder2");
        playerRegistry.register(p1, PlayerRole.ALIVE);
        playerRegistry.register(p2, PlayerRole.ALIVE);

        gameManager.transitionTo(new ActiveState(gameManager, playerRegistry, arenaService));

        Block block = p1.getWorld().getBlockAt(5, 100, 5);
        block.setType(Material.COBBLESTONE);

        // Fire BlockPlaceEvent
        org.bukkit.event.block.BlockPlaceEvent placeEvent = new org.bukkit.event.block.BlockPlaceEvent(
                block,
                block.getState(),
                p1.getWorld().getBlockAt(5, 99, 5),
                new org.bukkit.inventory.ItemStack(Material.COBBLESTONE),
                p1,
                true,
                org.bukkit.inventory.EquipmentSlot.HAND
        );
        server.getPluginManager().callEvent(placeEvent);

        assertFalse(placeEvent.isCancelled());
        assertTrue(arenaService.getModifiedBlocks().contains(block.getLocation()));

        // Clear arena
        arenaService.clearArena();

        assertEquals(Material.AIR, block.getType(), "Placed cobblestone should be cleared to AIR");
        assertTrue(arenaService.getModifiedBlocks().isEmpty());
    }

    @Test
    @DisplayName("Flowing liquid during ACTIVE state is tracked and cleared on arena reset")
    void testLiquidFlowTrackedAndCleared() {
        Player p1 = server.addPlayer("Builder1");
        Player p2 = server.addPlayer("Builder2");
        playerRegistry.register(p1, PlayerRole.ALIVE);
        playerRegistry.register(p2, PlayerRole.ALIVE);

        gameManager.transitionTo(new ActiveState(gameManager, playerRegistry, arenaService));

        Block fromBlock = p1.getWorld().getBlockAt(0, 100, 0);
        fromBlock.setType(Material.WATER);
        Block toBlock = p1.getWorld().getBlockAt(0, 99, 0);
        toBlock.setType(Material.WATER);

        org.bukkit.event.block.BlockFromToEvent flowEvent = new org.bukkit.event.block.BlockFromToEvent(
                fromBlock, toBlock
        );
        server.getPluginManager().callEvent(flowEvent);

        assertTrue(arenaService.getModifiedBlocks().contains(toBlock.getLocation()));

        arenaService.clearArena();

        assertEquals(Material.AIR, toBlock.getType(), "Flowing water should be cleared to AIR");
    }
}
