package space.nadamu.nadamupillar;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import space.nadamu.nadamupillar.arena.ProceduralArenaService;
import space.nadamu.nadamupillar.disaster.DisasterManager;
import space.nadamu.nadamupillar.domain.GamePlayer;
import space.nadamu.nadamupillar.domain.PlayerRole;
import space.nadamu.nadamupillar.loot.LootService;
import space.nadamu.nadamupillar.registry.PlayerRegistry;
import space.nadamu.nadamupillar.scheduler.ActionScheduler;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ActionSchedulerTest {
    private ServerMock server;
    private PlayerRegistry playerRegistry;
    private LootService lootService;
    private DisasterManager disasterManager;
    private ProceduralArenaService arenaService;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        playerRegistry = new PlayerRegistry();
        lootService = new LootService(new File("."), null);
        lootService.loadLoot();
        disasterManager = new DisasterManager();
        arenaService = new ProceduralArenaService();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("ActionScheduler ticks loot timer and distributes item to alive players")
    void testLootDistribution() {
        PlayerMock p1 = server.addPlayer();
        GamePlayer gp1 = playerRegistry.getOrCreatePlayer(p1);
        gp1.setRole(PlayerRole.ALIVE);

        // Fast loot interval: 1 second (20 ticks)
        ActionScheduler scheduler = new ActionScheduler(playerRegistry, lootService, disasterManager, arenaService, 1, 30);

        assertTrue(p1.getInventory().isEmpty());

        // Tick 20 times
        for (int i = 1; i <= 20; i++) {
            scheduler.tick(i);
        }

        assertFalse(p1.getInventory().isEmpty(), "Player should have received loot after 20 ticks");
    }

    @Test
    @DisplayName("ActionScheduler triggers disaster warning at 5s remaining")
    void testDisasterWarning() {
        PlayerMock p1 = server.addPlayer();
        GamePlayer gp1 = playerRegistry.getOrCreatePlayer(p1);
        gp1.setRole(PlayerRole.ALIVE);

        // Disaster interval 6 seconds (120 ticks), warning should trigger at 100 ticks remaining (tick 20)
        ActionScheduler scheduler = new ActionScheduler(playerRegistry, lootService, disasterManager, arenaService, 10, 6);

        assertNull(scheduler.getPendingDisaster());

        for (int i = 1; i <= 20; i++) {
            scheduler.tick(i);
        }

        assertEquals(100, scheduler.getRemainingDisasterTicks());
        assertNotNull(scheduler.getPendingDisaster(), "Disaster warning should be active at 100 ticks remaining");
    }
}
