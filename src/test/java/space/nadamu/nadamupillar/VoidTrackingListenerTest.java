package space.nadamu.nadamupillar;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import space.nadamu.nadamupillar.arena.ProceduralArenaService;
import space.nadamu.nadamupillar.domain.GamePlayer;
import space.nadamu.nadamupillar.domain.PlayerRole;
import space.nadamu.nadamupillar.fsm.ActiveState;
import space.nadamu.nadamupillar.fsm.GameManager;
import space.nadamu.nadamupillar.listener.VoidTrackingListener;
import space.nadamu.nadamupillar.registry.PlayerRegistry;

import static org.junit.jupiter.api.Assertions.*;

class VoidTrackingListenerTest {
    private ServerMock server;
    private PlayerRegistry playerRegistry;
    private ProceduralArenaService arenaService;
    private GameManager gameManager;
    private VoidTrackingListener listener;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        playerRegistry = new PlayerRegistry();
        arenaService = new ProceduralArenaService();
        gameManager = new GameManager(playerRegistry, arenaService, null);
        listener = new VoidTrackingListener(gameManager, playerRegistry, arenaService, -10.0);
        server.getPluginManager().registerEvents(listener, MockBukkit.createMockPlugin());
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Intercepts fatal damage and cancels death screen")
    void testInterceptFatalDamage() {
        Player player = server.addPlayer("Victim");
        GamePlayer gp = playerRegistry.register(player, PlayerRole.ALIVE);
        player.setHealth(10.0);

        // Fire fatal damage event
        EntityDamageEvent event = new EntityDamageEvent(player, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 15.0);
        server.getPluginManager().callEvent(event);

        assertTrue(event.isCancelled(), "Fatal damage event should be cancelled");
        assertFalse(gp.isAlive(), "GamePlayer should be marked eliminated");
        assertEquals(PlayerRole.SPECTATOR, gp.getRole());
        assertEquals(GameMode.SPECTATOR, player.getGameMode());
        assertEquals(player.getMaxHealth(), player.getHealth(), "Player health should be fully restored");
    }

    @Test
    @DisplayName("Non-fatal damage is not cancelled")
    void testNonFatalDamageNotCancelled() {
        Player player = server.addPlayer("Survivor");
        GamePlayer gp = playerRegistry.register(player, PlayerRole.ALIVE);
        player.setHealth(20.0);

        EntityDamageEvent event = new EntityDamageEvent(player, EntityDamageEvent.DamageCause.FALL, 5.0);
        server.getPluginManager().callEvent(event);

        assertFalse(event.isCancelled(), "Non-fatal damage should proceed normally");
        assertTrue(gp.isAlive());
        assertEquals(PlayerRole.ALIVE, gp.getRole());
    }

    @Test
    @DisplayName("Intercepts fall below void threshold")
    void testInterceptVoidFall() {
        Player player = server.addPlayer("Fallen");
        GamePlayer gp = playerRegistry.register(player, PlayerRole.ALIVE);

        Location from = new Location(player.getWorld(), 0, 5, 0);
        Location to = new Location(player.getWorld(), 0, -15, 0);
        PlayerMoveEvent moveEvent = new PlayerMoveEvent(player, from, to);
        server.getPluginManager().callEvent(moveEvent);

        assertFalse(gp.isAlive());
        assertEquals(PlayerRole.SPECTATOR, gp.getRole());
        assertEquals(GameMode.SPECTATOR, player.getGameMode());
    }
}
