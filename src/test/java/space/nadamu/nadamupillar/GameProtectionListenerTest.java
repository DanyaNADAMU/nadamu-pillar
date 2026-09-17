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
}
