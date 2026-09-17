package space.nadamu.nadamupillar;

import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import space.nadamu.nadamupillar.arena.BlockPatternParser;

import static org.junit.jupiter.api.Assertions.*;

class BlockPatternParserTest {

    @Test
    @DisplayName("Parses single material pattern")
    void testSingleMaterial() {
        BlockPatternParser parser = new BlockPatternParser("OBSIDIAN");
        assertEquals(Material.OBSIDIAN, parser.sample());
    }

    @Test
    @DisplayName("Parses weighted multi-block pattern")
    void testWeightedPattern() {
        BlockPatternParser parser = new BlockPatternParser("60%light_blue_concrete,40%cyan_concrete");

        boolean sawLightBlue = false;
        boolean sawCyan = false;

        for (int i = 0; i < 100; i++) {
            Material sampled = parser.sample();
            if (sampled == Material.LIGHT_BLUE_CONCRETE) sawLightBlue = true;
            if (sampled == Material.CYAN_CONCRETE) sawCyan = true;
        }

        assertTrue(sawLightBlue, "Expected to sample LIGHT_BLUE_CONCRETE");
        assertTrue(sawCyan, "Expected to sample CYAN_CONCRETE");
    }

    @Test
    @DisplayName("Falls back to stone on null or empty input")
    void testFallback() {
        BlockPatternParser parser1 = new BlockPatternParser("");
        assertEquals(Material.STONE, parser1.sample());

        BlockPatternParser parser2 = new BlockPatternParser(null);
        assertEquals(Material.STONE, parser2.sample());

        BlockPatternParser parser3 = new BlockPatternParser("INVALID_MATERIAL_NAME_XYZ");
        assertEquals(Material.STONE, parser3.sample());
    }
}
