package space.nadamu.nadamupillar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import space.nadamu.nadamupillar.arena.ProceduralArenaService;
import space.nadamu.nadamupillar.command.PillarsCommand;
import space.nadamu.nadamupillar.domain.GamePlayer;
import space.nadamu.nadamupillar.domain.PlayerRole;
import space.nadamu.nadamupillar.fsm.GameManager;
import space.nadamu.nadamupillar.registry.PlayerRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PillarsCommandTest {
    private ServerMock server;
    private PlayerRegistry playerRegistry;
    private ProceduralArenaService arenaService;
    private GameManager gameManager;
    private PillarsCommand command;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        playerRegistry = new PlayerRegistry();
        arenaService = new ProceduralArenaService();
        gameManager = new GameManager(playerRegistry, arenaService, null);
        command = new PillarsCommand(gameManager);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Command /pillars status reports current state")
    void testCommandStatus() {
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        boolean result = command.onCommand(admin, null, "pillars", new String[]{"status"});
        assertTrue(result);
    }

    @Test
    @DisplayName("Command /pillars start transitions state to STARTING")
    void testCommandStart() {
        PlayerMock p1 = server.addPlayer();
        PlayerMock p2 = server.addPlayer();
        p1.setOp(true);

        playerRegistry.getOrCreatePlayer(p1);
        playerRegistry.getOrCreatePlayer(p2);

        boolean result = command.onCommand(p1, null, "pillars", new String[]{"start"});
        assertTrue(result);
        assertEquals("STARTING", gameManager.getCurrentState().getName());
    }

    @Test
    @DisplayName("Command /pillars stop transitions state to WAITING and disables autostart")
    void testCommandStop() {
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        gameManager.startMatch(5);
        assertEquals("STARTING", gameManager.getCurrentState().getName());
        assertTrue(gameManager.isAutoStartEnabled());

        boolean result = command.onCommand(admin, null, "pillars", new String[]{"stop"});
        assertTrue(result);
        assertEquals("WAITING", gameManager.getCurrentState().getName());
        assertFalse(gameManager.isAutoStartEnabled(), "Auto-start should be disabled after /pillars stop");
    }

    @Test
    @DisplayName("Command /pillars autostart toggles and sets autostart state")
    void testCommandAutostart() {
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        // Turn off
        command.onCommand(admin, null, "pillars", new String[]{"autostart", "off"});
        assertFalse(gameManager.isAutoStartEnabled());

        // Turn on
        command.onCommand(admin, null, "pillars", new String[]{"autostart", "on"});
        assertTrue(gameManager.isAutoStartEnabled());

        // Toggle
        command.onCommand(admin, null, "pillars", new String[]{"autostart", "toggle"});
        assertFalse(gameManager.isAutoStartEnabled());
    }

    @Test
    @DisplayName("Tab completion suggests subcommands including autostart")
    void testTabCompletion() {
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        List<String> completions = command.onTabComplete(admin, null, "pillars", new String[]{""});
        assertNotNull(completions);
        assertTrue(completions.contains("start"));
        assertTrue(completions.contains("stop"));
        assertTrue(completions.contains("autostart"));
        assertTrue(completions.contains("status"));
        assertTrue(completions.contains("forcenext"));

        List<String> autoCompletions = command.onTabComplete(admin, null, "pillars", new String[]{"autostart", ""});
        assertNotNull(autoCompletions);
        assertTrue(autoCompletions.contains("on"));
        assertTrue(autoCompletions.contains("off"));
        assertTrue(autoCompletions.contains("toggle"));
    }
}
