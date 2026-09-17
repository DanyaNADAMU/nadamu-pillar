package space.nadamu.nadamupillar;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import space.nadamu.nadamupillar.disaster.*;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DisasterTest {
    private ServerMock server;
    private DisasterManager disasterManager;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        disasterManager = new DisasterManager();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("DisasterManager registers default disasters")
    void testDefaultDisasters() {
        assertFalse(disasterManager.getAllDisasters().isEmpty());
        assertTrue(disasterManager.getDisaster("meteor_shower").isPresent());
        assertTrue(disasterManager.getDisaster("anvil_rain").isPresent());
        assertTrue(disasterManager.getDisaster("ghast_assault").isPresent());
        assertTrue(disasterManager.getDisaster("wind_charge_storm").isPresent());
        assertTrue(disasterManager.getDisaster("levitation_wave").isPresent());
    }

    @Test
    @DisplayName("Filters disasters by allowed IDs")
    void testFilterAllowedIds() {
        List<Disaster> filtered = disasterManager.getAvailableDisasters(
                World.Environment.NORMAL,
                List.of("meteor_shower", "anvil_rain")
        );
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().anyMatch(d -> d.getId().equals("meteor_shower")));
        assertTrue(filtered.stream().anyMatch(d -> d.getId().equals("anvil_rain")));
    }

    @Test
    @DisplayName("Select random disaster returns matching disaster")
    void testSelectRandomDisaster() {
        Optional<Disaster> disaster = disasterManager.selectRandomDisaster(
                World.Environment.NORMAL,
                List.of("levitation_wave")
        );
        assertTrue(disaster.isPresent());
        assertEquals("levitation_wave", disaster.get().getId());
    }

    @Test
    @DisplayName("LevitationWaveDisaster applies potion effect to alive players")
    void testLevitationWaveDisasterExecution() {
        PlayerMock p1 = server.addPlayer();
        PlayerMock p2 = server.addPlayer();

        Disaster levitation = disasterManager.getDisaster("levitation_wave").orElseThrow();
        DisasterContext context = new DisasterContext(
                p1.getWorld(),
                List.of(p1, p2),
                p1.getLocation(),
                new Random()
        );

        levitation.execute(context);

        assertTrue(p1.getActivePotionEffects().stream()
                .anyMatch(pe -> pe.getType().getName().toLowerCase().contains("levitation")));
        assertTrue(p2.getActivePotionEffects().stream()
                .anyMatch(pe -> pe.getType().getName().toLowerCase().contains("levitation")));
    }

    @Test
    @DisplayName("MeteorShower and AnvilRain execute safely without uncaught exceptions")
    void testSafeExecutionUnderMockBukkit() {
        PlayerMock p1 = server.addPlayer();
        DisasterContext context = new DisasterContext(
                p1.getWorld(),
                List.of(p1),
                p1.getLocation(),
                new Random()
        );

        Disaster meteor = disasterManager.getDisaster("meteor_shower").orElseThrow();
        assertDoesNotThrow(() -> meteor.execute(context));

        Disaster anvil = disasterManager.getDisaster("anvil_rain").orElseThrow();
        assertDoesNotThrow(() -> anvil.execute(context));

        Disaster wind = disasterManager.getDisaster("wind_charge_storm").orElseThrow();
        assertDoesNotThrow(() -> wind.execute(context));
    }
}
