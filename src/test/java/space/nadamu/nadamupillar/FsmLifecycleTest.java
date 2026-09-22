package space.nadamu.nadamupillar;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import space.nadamu.nadamupillar.arena.ProceduralArenaService;
import space.nadamu.nadamupillar.domain.GamePlayer;
import space.nadamu.nadamupillar.domain.PlayerRole;
import space.nadamu.nadamupillar.fsm.*;
import space.nadamu.nadamupillar.registry.PlayerRegistry;

import static org.junit.jupiter.api.Assertions.*;

class FsmLifecycleTest {
    private ServerMock server;
    private PlayerRegistry playerRegistry;
    private ProceduralArenaService arenaService;
    private GameManager gameManager;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        playerRegistry = new PlayerRegistry();
        arenaService = new ProceduralArenaService();
        gameManager = new GameManager(playerRegistry, arenaService, null);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("FSM initializes in WAITING state")
    void testInitialState() {
        assertNotNull(gameManager.getCurrentState());
        assertEquals("WAITING", gameManager.getCurrentState().getName());
        assertFalse(gameManager.getCurrentState().canPvp());
        assertFalse(gameManager.getCurrentState().canBreakBlocks());
    }

    @Test
    @DisplayName("Manual start transitions to STARTING state in spectator mode")
    void testStartMatch() {
        Player p1 = server.addPlayer("Alice");
        Player p2 = server.addPlayer("Bob");
        playerRegistry.register(p1, PlayerRole.SPECTATOR);
        playerRegistry.register(p2, PlayerRole.SPECTATOR);

        gameManager.startMatch(5);

        assertEquals("STARTING", gameManager.getCurrentState().getName());
        assertFalse(gameManager.getCurrentState().canPvp());
        assertFalse(gameManager.getCurrentState().canBreakBlocks());
        assertEquals(2, playerRegistry.getTotalCount());
    }

    @Test
    @DisplayName("STARTING state transitions to ACTIVE and generates pillars for alive players")
    void testStartingToActiveTransition() {
        Player p1 = server.addPlayer("Alice");
        Player p2 = server.addPlayer("Bob");
        playerRegistry.register(p1, PlayerRole.SPECTATOR);
        playerRegistry.register(p2, PlayerRole.SPECTATOR);

        gameManager.startMatch(3); // 3 seconds = 60 ticks
        assertEquals("STARTING", gameManager.getCurrentState().getName());

        // Simulate 65 ticks so 60-tick countdown elapses
        for (int i = 0; i < 65; i++) {
            gameManager.tick();
        }

        assertEquals("ACTIVE", gameManager.getCurrentState().getName());
        assertTrue(gameManager.getCurrentState().canPvp());
        assertTrue(gameManager.getCurrentState().canBreakBlocks());
        assertEquals(2, playerRegistry.getAliveCount());
    }

    @Test
    @DisplayName("STARTING aborts and returns to WAITING if player count drops below minimum")
    void testStartingAbortsWhenPlayerLeaves() {
        Player p1 = server.addPlayer("Alice");
        Player p2 = server.addPlayer("Bob");
        playerRegistry.register(p1, PlayerRole.SPECTATOR);
        playerRegistry.register(p2, PlayerRole.SPECTATOR);

        gameManager.startMatch(5);
        assertEquals("STARTING", gameManager.getCurrentState().getName());

        // Player disconnects
        playerRegistry.unregister(p2.getUniqueId());
        gameManager.tick();

        assertEquals("WAITING", gameManager.getCurrentState().getName());
    }

    @Test
    @DisplayName("ACTIVE transitions to ENDING when <= 1 player alive")
    void testActiveToEndingWhenPlayersDie() {
        Player p1 = server.addPlayer("Alice");
        Player p2 = server.addPlayer("Bob");
        playerRegistry.register(p1, PlayerRole.SPECTATOR);
        playerRegistry.register(p2, PlayerRole.SPECTATOR);

        gameManager.transitionTo(new ActiveState(gameManager, playerRegistry, arenaService));
        assertEquals("ACTIVE", gameManager.getCurrentState().getName());
        assertEquals(2, playerRegistry.getAliveCount());

        // Eliminate p2
        GamePlayer gp2 = playerRegistry.getPlayer(p2).orElseThrow();
        gp2.setRole(PlayerRole.SPECTATOR);

        // Run ticks across the check interval (10 ticks)
        for (int i = 0; i < 15; i++) {
            gameManager.tick();
        }

        assertEquals("ENDING", gameManager.getCurrentState().getName());
        EndingState endingState = (EndingState) gameManager.getCurrentState();
        assertNotNull(endingState.getWinner());
        assertEquals("Alice", endingState.getWinner().getName());
    }

    @Test
    @DisplayName("ENDING returns to WAITING after celebration elapsed")
    void testEndingToWaitingTransition() {
        Player p1 = server.addPlayer("Alice");
        GamePlayer gp1 = playerRegistry.register(p1, PlayerRole.ALIVE);

        gameManager.transitionTo(new EndingState(gameManager, playerRegistry, arenaService, gp1, 2));
        assertEquals("ENDING", gameManager.getCurrentState().getName());

        // 2 seconds = 40 ticks
        for (int i = 0; i < 45; i++) {
            gameManager.tick();
        }

        assertEquals("WAITING", gameManager.getCurrentState().getName());
    }

    @Test
    @DisplayName("WAITING does not auto-start when autoStartEnabled is false")
    void testWaitingDoesNotAutoStartWhenDisabled() {
        Player p1 = server.addPlayer("Alice");
        Player p2 = server.addPlayer("Bob");
        playerRegistry.register(p1, PlayerRole.SPECTATOR);
        playerRegistry.register(p2, PlayerRole.SPECTATOR);

        gameManager.setAutoStartEnabled(false);

        // Tick 40 times (2 seconds)
        for (int i = 0; i < 40; i++) {
            gameManager.tick();
        }

        assertEquals("WAITING", gameManager.getCurrentState().getName(), "Should remain in WAITING when auto-start is disabled");

        // Enable auto-start and tick
        gameManager.setAutoStartEnabled(true);
        for (int i = 0; i < 25; i++) {
            gameManager.tick();
        }

        assertEquals("STARTING", gameManager.getCurrentState().getName(), "Should transition to STARTING when auto-start is enabled");
    }
}
