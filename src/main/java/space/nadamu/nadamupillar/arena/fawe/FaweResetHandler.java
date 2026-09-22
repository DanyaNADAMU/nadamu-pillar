package space.nadamu.nadamupillar.arena.fawe;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.World;

public final class FaweResetHandler {
    private FaweResetHandler() {}

    public static void clearRegion(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        var weWorld = BukkitAdapter.adapt(world);
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
            CuboidRegion region = new CuboidRegion(
                    weWorld,
                    BlockVector3.at(minX, minY, minZ),
                    BlockVector3.at(maxX, maxY, maxZ)
            );
            editSession.setBlocks((com.sk89q.worldedit.regions.Region) region, BlockTypes.AIR.getDefaultState());
        } catch (Exception e) {
            org.bukkit.Bukkit.getLogger().warning("FAWE clearRegion error: " + e.getMessage());
        }
    }
}
