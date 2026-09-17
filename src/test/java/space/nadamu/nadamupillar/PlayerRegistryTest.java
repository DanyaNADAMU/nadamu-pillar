package space.nadamu.nadamupillar;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import space.nadamu.nadamupillar.domain.GamePlayer;
import space.nadamu.nadamupillar.domain.PlayerRole;
import space.nadamu.nadamupillar.registry.PlayerRegistry;

import static org.junit.jupiter.api.Assertions.*;

class PlayerRegistryTest {
    private ServerMock server;
    private PlayerRegistry registry;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        registry = new PlayerRegistry();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Player registration and alive count tracking")
    void testRegisterAndAliveCount() {
        Player player1 = server.addPlayer("Player1");
        Player player2 = server.addPlayer("Player2");

        GamePlayer gp1 = registry.register(player1, PlayerRole.ALIVE);
        GamePlayer gp2 = registry.register(player2, PlayerRole.SPECTATOR);

        assertEquals(2, registry.getTotalCount());
        assertEquals(1, registry.getAliveCount());
        assertTrue(gp1.isAlive());
        assertFalse(gp2.isAlive());

        gp2.setRole(PlayerRole.ALIVE);
        assertEquals(2, registry.getAliveCount());

        registry.unregister(player1.getUniqueId());
        assertEquals(1, registry.getTotalCount());
        assertEquals(1, registry.getAliveCount());
    }

    @Test
    @DisplayName("Batch role mutation and clear")
    void testBatchRoleMutation() {
        Player player1 = server.addPlayer("P1");
        Player player2 = server.addPlayer("P2");

        registry.register(player1, PlayerRole.ALIVE);
        registry.register(player2, PlayerRole.ALIVE);
        assertEquals(2, registry.getAliveCount());

        registry.setAllRoles(PlayerRole.SPECTATOR);
        assertEquals(0, registry.getAliveCount());

        registry.clear();
        assertEquals(0, registry.getTotalCount());
    }
}
